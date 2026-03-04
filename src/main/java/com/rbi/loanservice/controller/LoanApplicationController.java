package com.rbi.loanservice.controller;

import com.rbi.loanservice.dto.LoanApplicationRequest;
import com.rbi.loanservice.dto.LoanApplicationResponse;
import com.rbi.loanservice.service.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
@Tag(name = "Loan Applications", description = "Submit and evaluate loan applications")
@SecurityRequirement(name = "bearerAuth") // this endpoint needs a JWT
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @Operation(
        summary = "Submit a loan application",
        description = "Evaluates eligibility, calculates EMI, and returns an approval offer or rejection with reasons"
    )
    @PostMapping
    public ResponseEntity<LoanApplicationResponse> apply(
            @Valid @RequestBody LoanApplicationRequest request) {

        LoanApplicationResponse response = loanApplicationService.process(request);
        return ResponseEntity.ok(response);
    }
}

