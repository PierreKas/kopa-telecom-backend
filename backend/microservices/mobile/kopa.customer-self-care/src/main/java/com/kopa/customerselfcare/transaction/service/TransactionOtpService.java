package com.kopa.customerselfcare.transaction.service;

import com.kopa.customerselfcare.common.exception.BadRequestException;
import com.kopa.customerselfcare.transaction.client.NotificationsClient;
import com.kopa.customerselfcare.transaction.client.OtpSendRequest;
import com.kopa.customerselfcare.transaction.client.OtpSendResponse;
import com.kopa.customerselfcare.transaction.model.Transaction;
import com.kopa.customerselfcare.transaction.model.TransactionStatus;
import com.kopa.customerselfcare.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** OTP generation/send/verify for OTP-gated transactions (FR-04) - recharge-by-amount and bundle
 *  purchase. Owns no sms_messages/history of its own, just the pending challenge state on the
 *  transaction row itself (see V1_6__transaction_otp_fields.sql). */
@Service
public class TransactionOtpService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionOtpService.class);

    private static final int OTP_LENGTH = 6;
    private static final int OTP_TTL_MINUTES = 5;
    private static final int OTP_MAX_ATTEMPTS = 5;

    private final NotificationsClient notificationsClient;
    private final TransactionRepository transactions;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    public TransactionOtpService(NotificationsClient notificationsClient, TransactionRepository transactions) {
        this.notificationsClient = notificationsClient;
        this.transactions = transactions;
    }

    /** Generates, hashes, stores, and dispatches an OTP for a transaction already in OTP_PENDING. */
    public void requestOtp(Transaction transaction) {
        String code = generateOtpCode();
        Instant now = Instant.now();

        transaction.setOtpCodeHash(passwordEncoder.encode(code));
        transaction.setOtpExpiresAt(now.plus(OTP_TTL_MINUTES, ChronoUnit.MINUTES));
        transaction.setOtpAttemptCount(0);
        transactions.save(transaction);

        // Only the BCrypt hash is persisted above - the plaintext code itself must never be
        // logged. The customer is the only one who should ever see it, via SMS.
        logger.info("Transaction OTP requested for {} (txn {})", transaction.getMsisdn(), transaction.getId());

        OtpSendResponse response = notificationsClient.sendOtp(new OtpSendRequest(
                transaction.getMsisdn(), code, "TRANSACTION_AUTH", transaction.getCorrelationId(), "customer-self-care"));
        if (!response.success()) {
            throw new BadRequestException("Could not send the verification code: " + response.errorMessage());
        }
    }

    /** Verifies the code against a transaction's pending OTP challenge. Throws on any failure -
     *  wrong code, expired, already consumed, or too many attempts - and increments the attempt
     *  counter on a wrong code. Does not itself change the transaction's status away from
     *  OTP_PENDING - the caller (RechargeServiceImpl/BundleServiceImpl) does that once it
     *  actually proceeds with the deferred KTRN call. */
    public void verify(Transaction transaction, String code) {
        if (transaction.getStatus() != TransactionStatus.OTP_PENDING) {
            throw new BadRequestException("This transaction is not waiting on a verification code.");
        }
        if (transaction.isOtpExpired(Instant.now())) {
            throw new BadRequestException("This verification code has expired.");
        }
        if (transaction.getOtpAttemptCount() >= OTP_MAX_ATTEMPTS) {
            throw new BadRequestException("Too many incorrect attempts. Start the transaction again.");
        }

        if (!passwordEncoder.matches(code, transaction.getOtpCodeHash())) {
            transaction.setOtpAttemptCount(transaction.getOtpAttemptCount() + 1);
            transactions.save(transaction);
            throw new BadRequestException("Incorrect verification code.");
        }
    }

    private String generateOtpCode() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int code = secureRandom.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", code);
    }
}
