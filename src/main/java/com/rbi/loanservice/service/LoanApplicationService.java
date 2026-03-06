package com.rbi.loanservice.service;

import com.rbi.loanservice.domain.ApplicationStatus;
import com.rbi.loanservice.domain.LoanApplication;
import com.rbi.loanservice.domain.RiskBand;
import com.rbi.loanservice.dto.*;
import com.rbi.loanservice.repository.JsonLoanApplicationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class LoanApplicationService {

    private final InterestRateService interestRateService;
    private final EmiCalculatorService emiCalculatorService;
    private final EligibilityService eligibilityService;
    private final JsonLoanApplicationRepository repository;

    public LoanApplicationService(InterestRateService interestRateService,
                                  EmiCalculatorService emiCalculatorService,
                                  EligibilityService eligibilityService,
                                  JsonLoanApplicationRepository repository) {
        this.interestRateService = interestRateService;
        this.emiCalculatorService = emiCalculatorService;
        this.eligibilityService = eligibilityService;
        this.repository = repository;
    }

    /**
     * Main entry point — takes a validated request, runs all rules,
     * persists the decision, and returns the response.
     */
    public LoanApplicationResponse process(LoanApplicationRequest request) {

        ApplicantRequest applicant = request.getApplicant();
        LoanRequest loan = request.getLoan();

        // Step 1: Classify the applicant's risk level based on credit score
        RiskBand riskBand = interestRateService.classifyRiskBand(applicant.getCreditScore());

        // Step 2: Derive the final interest rate for this risk profile
        BigDecimal rate = interestRateService.calculateInterestRate(
                riskBand, applicant.getEmploymentType(), loan.getAmount());

        // Step 3: Calculate EMI using the derived rate
        BigDecimal emi = emiCalculatorService.calculateEmi(loan.getAmount(), rate, loan.getTenureMonths());

        // Step 4: Run eligibility checks (credit score, age+tenure, EMI > 60%)
        List<String> rejectionReasons = eligibilityService.checkEligibility(
                applicant.getCreditScore(),
                applicant.getAge(),
                loan.getTenureMonths(),
                emi,
                applicant.getMonthlyIncome()
        );

        // Step 5: Even if eligibility passes, EMI must not exceed 50% of income for offer issuance
        if (rejectionReasons.isEmpty()) {
            BigDecimal fiftyPercentOfIncome = applicant.getMonthlyIncome()
                    .multiply(new BigDecimal("0.50"))
                    .setScale(2, RoundingMode.HALF_UP);

            if (emi.compareTo(fiftyPercentOfIncome) > 0) {
                rejectionReasons = List.of("EMI_EXCEEDS_50_PERCENT_OF_INCOME");
            }
        }

        // Step 6: Build entity and persist for audit
        LoanApplication entity = buildEntity(request, riskBand, rate, emi, rejectionReasons);
        entity = repository.save(entity);

        // Step 7: Return structured response
        if (rejectionReasons.isEmpty()) {
            BigDecimal totalPayable = emiCalculatorService.calculateTotalPayable(emi, loan.getTenureMonths());
            OfferResponse offer = new OfferResponse(rate, loan.getTenureMonths(), emi, totalPayable);
            return LoanApplicationResponse.approved(entity.getId(), riskBand, offer);
        } else {
            return LoanApplicationResponse.rejected(entity.getId(), rejectionReasons);
        }
    }

    private LoanApplication buildEntity(LoanApplicationRequest request,
                                        RiskBand riskBand,
                                        BigDecimal rate,
                                        BigDecimal emi,
                                        List<String> rejectionReasons) {
        ApplicantRequest applicant = request.getApplicant();
        LoanRequest loan = request.getLoan();

        LoanApplication entity = new LoanApplication();
        entity.setApplicantName(applicant.getName());
        entity.setApplicantAge(applicant.getAge());
        entity.setMonthlyIncome(applicant.getMonthlyIncome());
        entity.setEmploymentType(applicant.getEmploymentType());
        entity.setCreditScore(applicant.getCreditScore());
        entity.setLoanAmount(loan.getAmount());
        entity.setTenureMonths(loan.getTenureMonths());
        entity.setLoanPurpose(loan.getPurpose());

        if (rejectionReasons.isEmpty()) {
            entity.setStatus(ApplicationStatus.APPROVED);
            entity.setRiskBand(riskBand);
            entity.setInterestRate(rate);
            entity.setEmi(emi);
            entity.setTotalPayable(emiCalculatorService.calculateTotalPayable(emi, loan.getTenureMonths()));
        } else {
            entity.setStatus(ApplicationStatus.REJECTED);
            entity.setRejectionReasons(String.join(",", rejectionReasons));
        }

        return entity;
    }
}
