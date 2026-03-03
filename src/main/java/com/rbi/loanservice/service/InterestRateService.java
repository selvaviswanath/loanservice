package com.rbi.loanservice.service;

import com.rbi.loanservice.domain.EmploymentType;
import com.rbi.loanservice.domain.RiskBand;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class InterestRateService {

    // Base rate all applicants start with
    private static final BigDecimal BASE_RATE = new BigDecimal("12.0");

    // Loan size threshold above which a premium kicks in (₹10,00,000)
    private static final BigDecimal LARGE_LOAN_THRESHOLD = new BigDecimal("1000000");

    /**
     * Map credit score to a risk bucket.
     *   750+   → LOW
     *   650–749 → MEDIUM
     *   600–649 → HIGH
     *   Below 600 will be rejected upstream — should never reach here.
     */
    public RiskBand classifyRiskBand(int creditScore) {
        if (creditScore >= 750) return RiskBand.LOW;
        if (creditScore >= 650) return RiskBand.MEDIUM;
        return RiskBand.HIGH; // 600–649
    }

    /**
     * Final rate = base + risk premium + employment premium + loan size premium.
     */
    public BigDecimal calculateInterestRate(RiskBand riskBand, EmploymentType employmentType, BigDecimal loanAmount) {

        BigDecimal rate = BASE_RATE;

        // Risk premium: LOW → +0, MEDIUM → +1.5, HIGH → +3
        rate = rate.add(riskPremium(riskBand));

        // Self-employed applicants are slightly riskier → +1%
        if (employmentType == EmploymentType.SELF_EMPLOYED) {
            rate = rate.add(new BigDecimal("1.0"));
        }

        // Big loans carry more concentration risk → +0.5%
        if (loanAmount.compareTo(LARGE_LOAN_THRESHOLD) > 0) {
            rate = rate.add(new BigDecimal("0.5"));
        }

        return rate.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal riskPremium(RiskBand riskBand) {
        return switch (riskBand) {
            case LOW    -> BigDecimal.ZERO;
            case MEDIUM -> new BigDecimal("1.5");
            case HIGH   -> new BigDecimal("3.0");
        };
    }
}
