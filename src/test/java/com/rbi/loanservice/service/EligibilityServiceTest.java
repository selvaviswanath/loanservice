package com.rbi.loanservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityServiceTest {

    private EligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        eligibilityService = new EligibilityService();
    }

    @Test
    @DisplayName("All good — no rejection reasons returned")
    void eligible_applicant_noReasons() {
        List<String> reasons = eligibilityService.checkEligibility(
                700,                        // credit score — ok
                30,                         // age — ok
                36,                         // tenure (3 yrs) → age at end = 33 — ok
                new BigDecimal("15000"),    // EMI
                new BigDecimal("75000")     // income → 60% = 45000, EMI well below
        );
        assertThat(reasons).isEmpty();
    }

    @Test
    @DisplayName("Credit score below 600 → CREDIT_SCORE_TOO_LOW")
    void reject_lowCreditScore() {
        List<String> reasons = eligibilityService.checkEligibility(
                550, 30, 36,
                new BigDecimal("10000"),
                new BigDecimal("75000")
        );
        assertThat(reasons).contains(EligibilityService.REASON_LOW_CREDIT);
    }

    @Test
    @DisplayName("Age + tenure > 65 → AGE_TENURE_LIMIT_EXCEEDED")
    void reject_ageTenureLimitBreached() {
        // age 60 + 72 months (6 years) = 66 → over limit
        List<String> reasons = eligibilityService.checkEligibility(
                700, 60, 72,
                new BigDecimal("10000"),
                new BigDecimal("75000")
        );
        assertThat(reasons).contains(EligibilityService.REASON_AGE_TENURE);
    }

    @Test
    @DisplayName("Boundary: age 60 + 60 months (5 yrs) = exactly 65 — should pass")
    void boundary_agePlus5Years_exactlyAt65_passes() {
        List<String> reasons = eligibilityService.checkEligibility(
                700, 60, 60,
                new BigDecimal("10000"),
                new BigDecimal("75000")
        );
        assertThat(reasons).doesNotContain(EligibilityService.REASON_AGE_TENURE);
    }

    @Test
    @DisplayName("EMI > 60% of income → EMI_EXCEEDS_60_PERCENT")
    void reject_emiExceedsSixtyPercent() {
        // income = 50,000 → 60% = 30,000; EMI = 35,000 → reject
        List<String> reasons = eligibilityService.checkEligibility(
                700, 30, 36,
                new BigDecimal("35000"),
                new BigDecimal("50000")
        );
        assertThat(reasons).contains(EligibilityService.REASON_EMI_EXCEEDS_60);
    }

    @Test
    @DisplayName("All three rules violated — all three reasons returned")
    void reject_allThreeRules_allReasonsPresent() {
        // Score 550 (low), age 61 + 60 months = 66 (age breach), EMI 35K on 50K income (EMI breach)
        List<String> reasons = eligibilityService.checkEligibility(
                550, 61, 60,
                new BigDecimal("35000"),
                new BigDecimal("50000")
        );
        assertThat(reasons).containsExactlyInAnyOrder(
                EligibilityService.REASON_LOW_CREDIT,
                EligibilityService.REASON_AGE_TENURE,
                EligibilityService.REASON_EMI_EXCEEDS_60
        );
    }
}
