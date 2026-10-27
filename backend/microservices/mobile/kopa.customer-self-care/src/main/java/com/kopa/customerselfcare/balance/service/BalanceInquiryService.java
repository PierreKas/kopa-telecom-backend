package com.kopa.customerselfcare.balance.service;

import com.kopa.customerselfcare.balance.dto.BalanceDto;

public interface BalanceInquiryService {

    /** Feature 2.1.1 Searching Balance - svccode HP00001 / opcode HPOP001. */
    BalanceDto searchBalance(String msisdn, String ispCd);
}
