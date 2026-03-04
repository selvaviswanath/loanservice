package com.rbi.loanservice.controller;

import com.rbi.loanservice.domain.ApplicationStatus;
import com.rbi.loanservice.dto.*;
import com.rbi.loanservice.service.ApplicationQueryService;
import com.rbi.loanservice.service.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/applications")
@Tag(name = "Loan Applications", description = "Submit and evaluate loan applications")
@SecurityRequirement(name = "bearerAuth")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;
    private final ApplicationQueryService queryService;

    public LoanApplicationController(LoanApplicationService loanApplicationService,
                                     ApplicationQueryService queryService) {
        this.loanApplicationService = loanApplicationService;
        this.queryService = queryService;
    }

    @Operation(summary = "Submit a loan application",
               description = "Evaluates eligibility, calculates EMI, and returns an approval offer or rejection with reasons")
    @PostMapping
    public ResponseEntity<LoanApplicationResponse> apply(
            @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.ok(loanApplicationService.process(request));
    }

    @Operation(summary = "Get application by ID",
               description = "Retrieve a single loan decision by its UUID — useful for audit and frontend polling")
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationSummary> getById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(queryService.findById(id));
    }

    @Operation(summary = "List all applications (paginated)",
               description = "Audit view with optional filters. Sorted newest first.")
    @GetMapping
    public ResponseEntity<PagedResponse<ApplicationSummary>> list(
            @Parameter(description = "Filter by status: APPROVED or REJECTED")
            @RequestParam(required = false) ApplicationStatus status,

            @Parameter(description = "Filter from this date (inclusive), format: yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Filter up to this date (inclusive), format: yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(queryService.findAll(status, from, to, page, size));
    }
}

