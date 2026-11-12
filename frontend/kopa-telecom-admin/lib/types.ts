// Domain model for Kopa Telecom's admin portal - backed by the real kopa.admin-portal service
// (see lib/http/*). Every status enum below is a literal copy of the real backend's enum
// values (kopa.admin-portal's mirror.model.* classes) - keep these in sync if the backend adds
// a new value.

export interface User {
  id: string;
  username: string;
}

/** No token here by design - it lives only in an httpOnly cookie, server-side. */
export interface AuthSession {
  user: User;
  expiresAt?: string;
}

export type CustomerStatus = 'PENDING_ACTIVATION' | 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED';

export interface Customer {
  id: string;
  msisdn: string;
  nationalId: string | null;
  iccid: string | null;
  custId: string | null;
  fullName: string;
  birthDay: string | null;
  email: string | null;
  soId: string | null;
  status: CustomerStatus;
  createdAt: string;
  updatedAt: string;
}

export type TransactionType = 'RECHARGE' | 'RECHARGE_VOUCHER' | 'RECHARGE_CANCEL' | 'BUNDLE_PURCHASE';
export type TransactionStatus = 'INITIATED' | 'OTP_PENDING' | 'SUBMITTED' | 'SUCCESS' | 'FAILED' | 'EXPIRED';

export interface Transaction {
  id: string;
  msisdn: string;
  transactionType: TransactionType;
  productCode: string | null;
  amount: string | null;
  currency: string | null;
  status: TransactionStatus;
  ktrnMsgcode: string | null;
  ktrnMessage: string | null;
  failureLayer: string | null;
  failureDetail: string | null;
  requestedAt: string;
  completedAt: string | null;
  durationMs: number | null;
}

export type SmsMessageStatus = 'QUEUED' | 'SENT' | 'DELIVERED' | 'UNDELIVERED' | 'ERRORED' | 'FAILED_TO_SEND';

export interface SmsMessage {
  id: string;
  msisdn: string;
  purpose: string;
  templateCode: string;
  providerMessageId: string | null;
  status: SmsMessageStatus;
  cost: number | null;
  failureLayer: string | null;
  failureDetail: string | null;
  requestedByService: string;
  createdAt: string;
  updatedAt: string;
}

export interface DashboardSummary {
  newRegistrationsToday: number;
  transactionsToday: number;
  successfulTransactionsToday: number;
  failedTransactionsToday: number;
  smsSentToday: number;
  smsFailedToday: number;
  smsDeliveryRateToday: number;
}

export interface FailureBreakdownEntry {
  failureLayer: string;
  count: number;
}

export interface DashboardFailures {
  totalFailed: number;
  byLayer: FailureBreakdownEntry[];
}
