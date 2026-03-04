package com.rbi.loanservice.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Username must be unique across the system
    @Column(nullable = false, unique = true)
    private String username;

    // Stored as a BCrypt hash — never plain text
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // e.g. "ROLE_USER"

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
