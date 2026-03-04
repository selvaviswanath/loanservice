package com.rbi.loanservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Loan Evaluation API",
        version = "1.0",
        description = "Evaluates loan applications and returns approval decisions based on credit score, EMI affordability, and age limits. " +
                      "Register or login at /auth to get a Bearer token, then use it to call /applications."
    ),
    servers = @Server(url = "/", description = "Local")
)
// Tells Swagger to show a Bearer token input box on relevant endpoints
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Paste the JWT token from /auth/login here (without 'Bearer ' prefix)"
)
public class OpenApiConfig {
    // All configuration is via annotations — nothing extra needed here
}
