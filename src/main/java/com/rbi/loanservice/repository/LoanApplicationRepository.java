package com.rbi.loanservice.repository;

import com.rbi.loanservice.domain.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {
    // Standard CRUD is all we need — no custom queries for now
}
