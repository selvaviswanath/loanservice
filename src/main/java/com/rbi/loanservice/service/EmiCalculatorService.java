package com.rbi.loanservice.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Service
public class EmiCalculatorService {

    // All financial math uses HALF_UP rounding at scale 2 (paise level)
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Standard reducing-balance EMI formula:
     *   EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
     *
     * @param principal    loan amount
     * @param annualRatePercent  e.g. 13.5 for 13.5%
     * @param tenureMonths number of EMIs
     */
    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {

        // Convert annual % to monthly decimal: 13.5% / 12 / 100 = 0.01125
        BigDecimal monthlyRate = annualRatePercent
                .divide(BigDecimal.valueOf(12), 10, ROUNDING)
                .divide(BigDecimal.valueOf(100), 10, ROUNDING);

        // (1 + r)^n — need higher precision here to avoid compounding errors
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, new MathContext(15, ROUNDING));

        // numerator = P * r * (1+r)^n
        BigDecimal numerator = principal
                .multiply(monthlyRate)
                .multiply(onePlusRPowN);

        // denominator = (1+r)^n - 1
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, SCALE, ROUNDING);
    }

    /**
     * Total amount paid over the entire loan life.
     */
    public BigDecimal calculateTotalPayable(BigDecimal emi, int tenureMonths) {
        return emi.multiply(BigDecimal.valueOf(tenureMonths)).setScale(SCALE, ROUNDING);
    }
}
