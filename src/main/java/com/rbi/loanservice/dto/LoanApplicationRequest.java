package com.rbi.loanservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class LoanApplicationRequest {

    @NotNull(message = "Applicant details are required")
    @Valid // triggers nested validation on ApplicantRequest fields
    private ApplicantRequest applicant;

    @NotNull(message = "Loan details are required")
    @Valid // triggers nested validation on LoanRequest fields
    private LoanRequest loan;

    // — Getters & Setters —

    public ApplicantRequest getApplicant() { return applicant; }
    public void setApplicant(ApplicantRequest applicant) { this.applicant = applicant; }

    public LoanRequest getLoan() { return loan; }
    public void setLoan(LoanRequest loan) { this.loan = loan; }
}
