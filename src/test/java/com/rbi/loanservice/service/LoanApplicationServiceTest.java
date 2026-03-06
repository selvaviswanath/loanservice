package com.rbi.loanservice.service;

import com.rbi.loanservice.domain.ApplicationStatus;
import com.rbi.loanservice.domain.LoanApplication;
import com.rbi.loanservice.domain.RiskBand;
import com.rbi.loanservice.dto.*;
import com.rbi.loanservice.repository.JsonLoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    // Real services — no mocking needed, they're pure logic with no external deps
    private EmiCalculatorService emiCalculatorService;
    private InterestRateService interestRateService;
    private EligibilityService eligibilityService;

    @Mock
    private JsonLoanApplicationRepository repository;

    private LoanApplicationService loanApplicationService;

    @BeforeEach
    void setUp() {
        emiCalculatorService = new EmiCalculatorService();
        interestRateService = new InterestRateService();
        eligibilityService = new EligibilityService();

        loanApplicationService = new LoanApplicationService(
                interestRateService, emiCalculatorService, eligibilityService, repository);

        // Stub save() to return the entity with a fake UUID
        when(repository.save(any(LoanApplication.class))).thenAnswer(inv -> {
            LoanApplication app = inv.getArgument(0);
            // Set an ID via reflection isn't ideal — instead we just check the response isn't null
            return app;
        });
    }

    @Test
    @DisplayName("Happy path: good applicant gets APPROVED with an offer")
    void process_goodApplicant_approved() {
        LoanApplicationRequest request = buildRequest(
                "Alice", 30, new BigDecimal("75000"), "SALARIED", 720,
                new BigDecimal("500000"), 36, "PERSONAL"
        );

        LoanApplicationResponse response = loanApplicationService.process(request);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(response.getRiskBand()).isEqualTo(RiskBand.MEDIUM); // 720 → MEDIUM
        assertThat(response.getOffer()).isNotNull();
        assertThat(response.getOffer().getEmi()).isPositive();
        assertThat(response.getOffer().getTotalPayable()).isGreaterThan(new BigDecimal("500000"));
        assertThat(response.getRejectionReasons()).isNull();
    }

    @Test
    @DisplayName("Credit score below 600 → REJECTED with CREDIT_SCORE_TOO_LOW")
    void process_lowCreditScore_rejected() {
        LoanApplicationRequest request = buildRequest(
                "Bob", 30, new BigDecimal("75000"), "SALARIED", 550,
                new BigDecimal("500000"), 36, "PERSONAL"
        );

        LoanApplicationResponse response = loanApplicationService.process(request);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(response.getRejectionReasons()).contains(EligibilityService.REASON_LOW_CREDIT);
        assertThat(response.getOffer()).isNull();
    }

    @Test
    @DisplayName("Age + tenure breach → REJECTED with AGE_TENURE_LIMIT_EXCEEDED")
    void process_ageTenureBreach_rejected() {
        // Age 60 + 72 months = 66 → over limit
        LoanApplicationRequest request = buildRequest(
                "Carol", 60, new BigDecimal("200000"), "SALARIED", 700,
                new BigDecimal("500000"), 72, "HOME"
        );

        LoanApplicationResponse response = loanApplicationService.process(request);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(response.getRejectionReasons()).contains(EligibilityService.REASON_AGE_TENURE);
    }

    @Test
    @DisplayName("EMI too high (>50% income) → rejected even if all eligibility checks pass")
    void process_emiExceedsFiftyPercent_rejected() {
        // Small income, very large loan with high tenure — EMI will blow past 50%
        LoanApplicationRequest request = buildRequest(
                "Dave", 30, new BigDecimal("20000"), "SALARIED", 760,
                new BigDecimal("800000"), 12, "PERSONAL"
        );

        LoanApplicationResponse response = loanApplicationService.process(request);

        // LOW risk (760), SALARIED, ≤10L → rate = 12%
        // EMI on 8L at 12% for 12 months ≈ ₹71,073 — way above 50% of ₹20K = ₹10K
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(response.getRejectionReasons()).isNotEmpty();
    }

    @Test
    @DisplayName("Entity is saved to repository on every request (approved or rejected)")
    void process_alwaysPersistsToRepository() {
        LoanApplicationRequest request = buildRequest(
                "Eve", 30, new BigDecimal("75000"), "SALARIED", 720,
                new BigDecimal("500000"), 36, "PERSONAL"
        );

        loanApplicationService.process(request);

        // Verify audit save happened exactly once
        verify(repository, times(1)).save(any(LoanApplication.class));
    }

    // ——— Helper builder ———

    private LoanApplicationRequest buildRequest(
            String name, int age, BigDecimal income, String employment, int creditScore,
            BigDecimal amount, int tenure, String purpose) {

        ApplicantRequest applicant = new ApplicantRequest();
        applicant.setName(name);
        applicant.setAge(age);
        applicant.setMonthlyIncome(income);
        applicant.setEmploymentType(com.rbi.loanservice.domain.EmploymentType.valueOf(employment));
        applicant.setCreditScore(creditScore);

        LoanRequest loan = new LoanRequest();
        loan.setAmount(amount);
        loan.setTenureMonths(tenure);
        loan.setPurpose(com.rbi.loanservice.domain.LoanPurpose.valueOf(purpose));

        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setApplicant(applicant);
        request.setLoan(loan);
        return request;
    }
}
