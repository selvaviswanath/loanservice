package com.rbi.loanservice.dto;

import com.rbi.loanservice.domain.EmploymentType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ApplicantRequest {

    @NotBlank(message = "Name is required")
    private String name;

    // Must be 21–60 to be eligible for a loan
    @Min(value = 21, message = "Applicant must be at least 21 years old")
    @Max(value = 60, message = "Applicant must be 60 years old or younger")
    private int age;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.01", message = "Monthly income must be greater than 0")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Employment type is required (SALARIED or SELF_EMPLOYED)")
    private EmploymentType employmentType;

    // Credit score range: 300 (terrible) to 900 (excellent)
    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 900, message = "Credit score cannot exceed 900")
    private int creditScore;

    // — Getters & Setters —

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }

    public int getCreditScore() { return creditScore; }
    public void setCreditScore(int creditScore) { this.creditScore = creditScore; }
}
