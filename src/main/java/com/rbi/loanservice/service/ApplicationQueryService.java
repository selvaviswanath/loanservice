package com.rbi.loanservice.service;

import com.rbi.loanservice.domain.ApplicationStatus;
import com.rbi.loanservice.dto.ApplicationSummary;
import com.rbi.loanservice.dto.PagedResponse;
import com.rbi.loanservice.repository.JsonLoanApplicationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ApplicationQueryService {

    private final JsonLoanApplicationRepository repository;

    public ApplicationQueryService(JsonLoanApplicationRepository repository) {
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

        // Convert date boundaries to LocalDateTime (start of day / end of day)
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt   = to   != null ? to.atTime(23, 59, 59) : null;

        List<ApplicationSummary> content = repository
                .findWithFilters(status, fromDt, toDt, page, size)
                .stream()
                .map(ApplicationSummary::from)
                .toList();

        long total = repository.countWithFilters(status, fromDt, toDt);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);

        return new PagedResponse<>(content, page, size, total, totalPages);
    }

    /** Thrown when an application ID is not found — maps to 404. */
    public static class ApplicationNotFoundException extends RuntimeException {
        public ApplicationNotFoundException(UUID id) {
            super("Application not found: " + id);
        }
    }
}
