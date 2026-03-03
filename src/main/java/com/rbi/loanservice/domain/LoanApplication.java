package com.rbi.loanservice.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // — Applicant info —
    private String applicantName;
    private int applicantAge;
    private BigDecimal monthlyIncome;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    private int creditScore;

    // — Loan info —
    private BigDecimal loanAmount;
    private int tenureMonths;

    @Enumerated(EnumType.STRING)
    private LoanPurpose loanPurpose;

    // — Decision —
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    private RiskBand riskBand; // null when rejected

    private BigDecimal interestRate; // null when rejected
    private BigDecimal emi;          // null when rejected
    private BigDecimal totalPayable; // null when rejected

    // Rejection reasons stored as comma-separated string (e.g. "CREDIT_SCORE_TOO_LOW,AGE_TENURE_LIMIT_EXCEEDED")
    @Column(length = 500)
    private String rejectionReasons;

    private LocalDateTime createdAt;

    @PrePersist
    void onPersist() {
        createdAt = LocalDateTime.now();
    }

    // — Getters & Setters —

    public UUID getId() { return id; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public int getApplicantAge() { return applicantAge; }
    public void setApplicantAge(int applicantAge) { this.applicantAge = applicantAge; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }

    public int getCreditScore() { return creditScore; }
    public void setCreditScore(int creditScore) { this.creditScore = creditScore; }

    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }

    public int getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(int tenureMonths) { this.tenureMonths = tenureMonths; }

    public LoanPurpose getLoanPurpose() { return loanPurpose; }
    public void setLoanPurpose(LoanPurpose loanPurpose) { this.loanPurpose = loanPurpose; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public RiskBand getRiskBand() { return riskBand; }
    public void setRiskBand(RiskBand riskBand) { this.riskBand = riskBand; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public BigDecimal getEmi() { return emi; }
    public void setEmi(BigDecimal emi) { this.emi = emi; }

    public BigDecimal getTotalPayable() { return totalPayable; }
    public void setTotalPayable(BigDecimal totalPayable) { this.totalPayable = totalPayable; }

    public String getRejectionReasons() { return rejectionReasons; }
    public void setRejectionReasons(String rejectionReasons) { this.rejectionReasons = rejectionReasons; }

    // Convenience: split comma-separated reasons back into a list
    public List<String> getRejectionReasonsList() {
        if (rejectionReasons == null || rejectionReasons.isBlank()) return List.of();
        return List.of(rejectionReasons.split(","));
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
