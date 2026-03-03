package com.rbi.loanservice.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class EligibilityService {

    // Rejection reason constants — used in response and stored in DB
    public static final String REASON_LOW_CREDIT        = "CREDIT_SCORE_TOO_LOW";
    public static final String REASON_AGE_TENURE        = "AGE_TENURE_LIMIT_EXCEEDED";
    public static final String REASON_EMI_EXCEEDS_60    = "EMI_EXCEEDS_60_PERCENT";

    // We cap working life at 65 — loan must end before applicant turns 65
    private static final int MAX_AGE_AT_LOAN_END = 65;

    // EMI should not consume more than 60% of take-home pay
    private static final BigDecimal MAX_EMI_TO_INCOME_RATIO = new BigDecimal("0.60");

    /**
     * Runs all three eligibility checks and collects every failure reason.
     * An empty list means the applicant is eligible.
     *
     * @param creditScore    applicant's credit score
     * @param age            applicant's current age
     * @param tenureMonths   requested loan tenure
     * @param emi            already calculated EMI for this loan
     * @param monthlyIncome  applicant's monthly income
     */
    public List<String> checkEligibility(int creditScore, int age, int tenureMonths,
                                         BigDecimal emi, BigDecimal monthlyIncome) {
        List<String> reasons = new ArrayList<>();

        // Rule 1: credit score below 600 is an automatic reject
        if (creditScore < 600) {
            reasons.add(REASON_LOW_CREDIT);
        }

        // Rule 2: applicant's age at loan end must not exceed 65
        // e.g. age 40 + 36 months (3 years) = 43 → ok; age 63 + 36 months = 66 → reject
        double ageAtEnd = age + (tenureMonths / 12.0);
        if (ageAtEnd > MAX_AGE_AT_LOAN_END) {
            reasons.add(REASON_AGE_TENURE);
        }

        // Rule 3: EMI cannot exceed 60% of monthly income (affordability check)
        BigDecimal maxAllowedEmi = monthlyIncome.multiply(MAX_EMI_TO_INCOME_RATIO);
        if (emi.compareTo(maxAllowedEmi) > 0) {
            reasons.add(REASON_EMI_EXCEEDS_60);
        }

        return reasons;
    }
}
