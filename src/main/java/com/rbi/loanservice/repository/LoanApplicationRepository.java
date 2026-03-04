package com.rbi.loanservice.repository;

import com.rbi.loanservice.domain.ApplicationStatus;
import com.rbi.loanservice.domain.LoanApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    // Filter by status only
    Page<LoanApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    // Filter by date range only
    Page<LoanApplication> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    // Filter by both status and date range
    Page<LoanApplication> findByStatusAndCreatedAtBetween(
            ApplicationStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable);

    // All records paged — used when no filters are provided
    @Query("SELECT a FROM LoanApplication a WHERE " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:from IS NULL OR a.createdAt >= :from) AND " +
           "(:to IS NULL OR a.createdAt <= :to)")
    Page<LoanApplication> findWithFilters(@Param("status") ApplicationStatus status,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          Pageable pageable);
}
