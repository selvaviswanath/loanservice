package com.rbi.loanservice.repository;

import com.rbi.loanservice.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    // Used during login and JWT filter to look up the user
    Optional<AppUser> findByUsername(String username);

    // Quick existence check during registration to prevent duplicates
    boolean existsByUsername(String username);
}
