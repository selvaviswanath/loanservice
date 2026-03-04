package com.rbi.loanservice.service;

import com.rbi.loanservice.domain.EmploymentType;
import com.rbi.loanservice.domain.RiskBand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InterestRateServiceTest {

    private InterestRateService interestRateService;

    @BeforeEach
    void setUp() {
        interestRateService = new InterestRateService();
    }

    // ——— Risk Band Classification ———

    @Test
    @DisplayName("Score 750+ → LOW risk")
    void classify_750_isLow() {
        assertThat(interestRateService.classifyRiskBand(750)).isEqualTo(RiskBand.LOW);
        assertThat(interestRateService.classifyRiskBand(800)).isEqualTo(RiskBand.LOW);
        assertThat(interestRateService.classifyRiskBand(900)).isEqualTo(RiskBand.LOW);
    }

    @Test
    @DisplayName("Score 650–749 → MEDIUM risk")
    void classify_650to749_isMedium() {
        assertThat(interestRateService.classifyRiskBand(650)).isEqualTo(RiskBand.MEDIUM);
        assertThat(interestRateService.classifyRiskBand(700)).isEqualTo(RiskBand.MEDIUM);
        assertThat(interestRateService.classifyRiskBand(749)).isEqualTo(RiskBand.MEDIUM);
    }

    @Test
    @DisplayName("Score 600–649 → HIGH risk")
    void classify_600to649_isHigh() {
        assertThat(interestRateService.classifyRiskBand(600)).isEqualTo(RiskBand.HIGH);
        assertThat(interestRateService.classifyRiskBand(620)).isEqualTo(RiskBand.HIGH);
        assertThat(interestRateService.classifyRiskBand(649)).isEqualTo(RiskBand.HIGH);
    }

    // ——— Interest Rate Calculation ———

    @Test
    @DisplayName("LOW risk, SALARIED, small loan → base 12% only")
    void rate_lowRisk_salaried_smallLoan() {
        BigDecimal rate = interestRateService.calculateInterestRate(
                RiskBand.LOW, EmploymentType.SALARIED, new BigDecimal("500000"));
        // 12 + 0 (LOW) + 0 (SALARIED) + 0 (≤10L) = 12%
        assertThat(rate).isEqualByComparingTo(new BigDecimal("12.00"));
    }

    @Test
    @DisplayName("MEDIUM risk, SALARIED, small loan → 13.5%")
    void rate_mediumRisk_salaried_smallLoan() {
        BigDecimal rate = interestRateService.calculateInterestRate(
                RiskBand.MEDIUM, EmploymentType.SALARIED, new BigDecimal("500000"));
        // 12 + 1.5 (MEDIUM) + 0 + 0 = 13.5%
        assertThat(rate).isEqualByComparingTo(new BigDecimal("13.50"));
    }

    @Test
    @DisplayName("HIGH risk, SELF_EMPLOYED, large loan → 16.5%")
    void rate_highRisk_selfEmployed_largeLoan() {
        BigDecimal rate = interestRateService.calculateInterestRate(
                RiskBand.HIGH, EmploymentType.SELF_EMPLOYED, new BigDecimal("1500000"));
        // 12 + 3 (HIGH) + 1 (SELF_EMPLOYED) + 0.5 (>10L) = 16.5%
        assertThat(rate).isEqualByComparingTo(new BigDecimal("16.50"));
    }

    @Test
    @DisplayName("LOW risk, SELF_EMPLOYED, large loan → 13.5%")
    void rate_lowRisk_selfEmployed_largeLoan() {
        BigDecimal rate = interestRateService.calculateInterestRate(
                RiskBand.LOW, EmploymentType.SELF_EMPLOYED, new BigDecimal("2000000"));
        // 12 + 0 + 1 + 0.5 = 13.5%
        assertThat(rate).isEqualByComparingTo(new BigDecimal("13.50"));
    }

    @Test
    @DisplayName("Loan amount exactly at 10L threshold — no size premium")
    void rate_exactlyAtThreshold_noSizePremium() {
        BigDecimal rate = interestRateService.calculateInterestRate(
                RiskBand.LOW, EmploymentType.SALARIED, new BigDecimal("1000000"));
        // Exactly 10L — premium is only for amounts ABOVE 10L
        assertThat(rate).isEqualByComparingTo(new BigDecimal("12.00"));
    }
}
