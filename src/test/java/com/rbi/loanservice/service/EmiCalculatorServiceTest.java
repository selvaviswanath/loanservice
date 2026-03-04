package com.rbi.loanservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EmiCalculatorServiceTest {

    private EmiCalculatorService emiCalculatorService;

    @BeforeEach
    void setUp() {
        emiCalculatorService = new EmiCalculatorService();
    }

    @Test
    @DisplayName("Standard case: ₹5L at 13.5% for 36 months")
    void calculateEmi_standardCase() {
        BigDecimal emi = emiCalculatorService.calculateEmi(
                new BigDecimal("500000"),
                new BigDecimal("13.5"),
                36
        );
        // Expected EMI verified against actual formula output (scale=2, HALF_UP)
        assertThat(emi).isEqualByComparingTo(new BigDecimal("16967.64"));
    }

    @Test
    @DisplayName("Short tenure: ₹1L at 12% for 6 months")
    void calculateEmi_shortTenure() {
        BigDecimal emi = emiCalculatorService.calculateEmi(
                new BigDecimal("100000"),
                new BigDecimal("12.0"),
                6
        );
        // Monthly rate = 1%, 6 payments
        assertThat(emi).isEqualByComparingTo(new BigDecimal("17254.84"));
    }

    @Test
    @DisplayName("Long tenure: ₹20L at 15% for 240 months")
    void calculateEmi_longTenure() {
        BigDecimal emi = emiCalculatorService.calculateEmi(
                new BigDecimal("2000000"),
                new BigDecimal("15.0"),
                240
        );
        // High rate over 20 years — EMI should be reasonable
        assertThat(emi).isPositive();
        // Total must be significantly more than principal — lots of interest
        BigDecimal total = emiCalculatorService.calculateTotalPayable(emi, 240);
        assertThat(total).isGreaterThan(new BigDecimal("2000000"));
    }

    @Test
    @DisplayName("Total payable = EMI × tenure")
    void calculateTotalPayable_correctMultiplication() {
        BigDecimal emi = new BigDecimal("16000.00");
        BigDecimal total = emiCalculatorService.calculateTotalPayable(emi, 36);
        assertThat(total).isEqualByComparingTo(new BigDecimal("576000.00"));
    }

    @Test
    @DisplayName("Minimum loan amount: ₹10,000 at 12% for 6 months")
    void calculateEmi_minimumLoan() {
        BigDecimal emi = emiCalculatorService.calculateEmi(
                new BigDecimal("10000"),
                new BigDecimal("12.0"),
                6
        );
        assertThat(emi).isPositive();
        // EMI should be less than the loan amount itself for a short tenure
        assertThat(emi).isLessThan(new BigDecimal("10000"));
    }
}
