package com.rbi.loanservice.service;

import com.rbi.loanservice.domain.AppUser;
import com.rbi.loanservice.dto.AuthRequest;
import com.rbi.loanservice.dto.AuthResponse;
import com.rbi.loanservice.repository.UserRepository;
import com.rbi.loanservice.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /** Register a new user — fails fast if the username is already taken. */
    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt hash
        user.setRole("ROLE_USER");

        userRepository.save(user);

        String token = jwtService.generateToken(request.getUsername());
        return new AuthResponse(token, "Registration successful");
    }

    /** Authenticate an existing user and return a fresh JWT. */
    public AuthResponse login(AuthRequest request) {
        // Spring Security handles credential verification — throws on bad creds
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtService.generateToken(request.getUsername());
        return new AuthResponse(token, "Login successful");
    }
}
