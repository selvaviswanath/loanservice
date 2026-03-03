package com.rbi.loanservice.dto;

import com.rbi.loanservice.domain.LoanPurpose;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class LoanRequest {

    // 10,000 minimum — 50,00,000 maximum
    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "10000", message = "Loan amount must be at least ₹10,000")
    @DecimalMax(value = "5000000", message = "Loan amount cannot exceed ₹50,00,000")
    private BigDecimal amount;

    // 6 months minimum — 30 years (360 months) maximum
    @Min(value = 6, message = "Tenure must be at least 6 months")
    @Max(value = 360, message = "Tenure cannot exceed 360 months (30 years)")
    private int tenureMonths;

    @NotNull(message = "Loan purpose is required (PERSONAL, HOME, or AUTO)")
    private LoanPurpose purpose;

    // — Getters & Setters —

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public int getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(int tenureMonths) { this.tenureMonths = tenureMonths; }

    public LoanPurpose getPurpose() { return purpose; }
    public void setPurpose(LoanPurpose purpose) { this.purpose = purpose; }
}
