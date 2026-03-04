package com.rbi.loanservice.controller;

import com.rbi.loanservice.dto.LoanApplicationRequest;
import com.rbi.loanservice.dto.LoanApplicationResponse;
import com.rbi.loanservice.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    /**
     * Submit a new loan application.
     * @Valid triggers all nested DTO validation before the service is called.
     */
    @PostMapping
    public ResponseEntity<LoanApplicationResponse> apply(
            @Valid @RequestBody LoanApplicationRequest request) {

        LoanApplicationResponse response = loanApplicationService.process(request);
        return ResponseEntity.ok(response);
    }
}
