package com.rbi.loanservice.dto;

import com.rbi.loanservice.domain.ApplicationStatus;
import com.rbi.loanservice.domain.RiskBand;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Compact view of a single application — used in both GET /applications
 * list responses and GET /applications/{id}.
 */
public class ApplicationSummary {

    private UUID id;
    private String applicantName;
    private BigDecimal loanAmount;
    private int tenureMonths;
    private ApplicationStatus status;
    private RiskBand riskBand;
    private BigDecimal interestRate;
    private BigDecimal emi;
    private BigDecimal totalPayable;
    private List<String> rejectionReasons;
    private LocalDateTime createdAt;

    // Factory: builds from the JPA entity
    public static ApplicationSummary from(com.rbi.loanservice.domain.LoanApplication e) {
        ApplicationSummary s = new ApplicationSummary();
        s.id = e.getId();
        s.applicantName = e.getApplicantName();
        s.loanAmount = e.getLoanAmount();
        s.tenureMonths = e.getTenureMonths();
        s.status = e.getStatus();
        s.riskBand = e.getRiskBand();
        s.interestRate = e.getInterestRate();
        s.emi = e.getEmi();
        s.totalPayable = e.getTotalPayable();
        s.rejectionReasons = e.getRejectionReasonsList();
        s.createdAt = e.getCreatedAt();
        return s;
    }

    public UUID getId() { return id; }
    public String getApplicantName() { return applicantName; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public int getTenureMonths() { return tenureMonths; }
    public ApplicationStatus getStatus() { return status; }
    public RiskBand getRiskBand() { return riskBand; }
    public BigDecimal getInterestRate() { return interestRate; }
    public BigDecimal getEmi() { return emi; }
    public BigDecimal getTotalPayable() { return totalPayable; }
    public List<String> getRejectionReasons() { return rejectionReasons; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
