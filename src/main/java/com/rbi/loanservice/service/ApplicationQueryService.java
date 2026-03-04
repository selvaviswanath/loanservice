package com.rbi.loanservice.service;

import com.rbi.loanservice.domain.ApplicationStatus;
import com.rbi.loanservice.dto.ApplicationSummary;
import com.rbi.loanservice.dto.PagedResponse;
import com.rbi.loanservice.repository.LoanApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ApplicationQueryService {

    private final LoanApplicationRepository repository;

    public ApplicationQueryService(LoanApplicationRepository repository) {
        this.repository = repository;
    }

    /** Fetch a single application by ID — used for audit and frontend polling. */
    public ApplicationSummary findById(UUID id) {
        return repository.findById(id)
                .map(ApplicationSummary::from)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    /**
     * Paginated list with optional filters.
     * Sorted newest first so audit logs are easy to scan.
     */
    public PagedResponse<ApplicationSummary> findAll(
            ApplicationStatus status,
            LocalDate from,
            LocalDate to,
            int page,
            int size) {

        // Clamp page size to avoid abuse
        size = Math.min(size, 100);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Convert date boundaries to LocalDateTime (start of day / end of day)
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt   = to   != null ? to.atTime(23, 59, 59) : null;

        Page<ApplicationSummary> result = repository
                .findWithFilters(status, fromDt, toDt, pageable)
                .map(ApplicationSummary::from);

        return new PagedResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /** Thrown when an application ID is not found — maps to 404. */
    public static class ApplicationNotFoundException extends RuntimeException {
        public ApplicationNotFoundException(UUID id) {
            super("Application not found: " + id);
        }
    }
}
