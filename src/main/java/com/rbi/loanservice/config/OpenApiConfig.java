package com.rbi.loanservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Loan Evaluation API",
        version = "1.0",
        description = "Evaluates loan applications and returns approval decisions based on credit score, EMI affordability, and age limits."
    ),
    servers = @Server(url = "/", description = "Local")
)
public class OpenApiConfig {
    // All configuration is via annotations — nothing extra needed here
}
