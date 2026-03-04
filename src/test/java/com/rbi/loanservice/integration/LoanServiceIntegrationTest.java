package com.rbi.loanservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbi.loanservice.dto.AuthRequest;
import com.rbi.loanservice.dto.LoanApplicationRequest;
import com.rbi.loanservice.dto.ApplicantRequest;
import com.rbi.loanservice.dto.LoanRequest;
import com.rbi.loanservice.domain.EmploymentType;
import com.rbi.loanservice.domain.LoanPurpose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests — starts full Spring context with H2.
 * Tests the entire HTTP stack: filters, security, validation, and response shapes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoanServiceIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String jwtToken;

    // Register a fresh user before each test to avoid state leakage
    @BeforeEach
    void setup() throws Exception {
        AuthRequest auth = new AuthRequest();
        auth.setUsername("integrationuser_" + System.currentTimeMillis());
        auth.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(responseBody).get("token").asText();
    }

    // — Auth Tests —

    @Test
    @DisplayName("Register with duplicate username returns 409")
    void register_duplicate_returns409() throws Exception {
        AuthRequest auth = new AuthRequest();
        auth.setUsername("duplicateuser");
        auth.setPassword("password123");

        // First registration
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(auth)))
                .andExpect(status().isOk());

        // Second should conflict
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(auth)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("Login with wrong password returns 401")
    void login_wrongPassword_returns401() throws Exception {
        AuthRequest auth = new AuthRequest();
        auth.setUsername("nonexistent");
        auth.setPassword("wrongpass");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(auth)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Register with short password returns 400")
    void register_shortPassword_returns400() throws Exception {
        AuthRequest auth = new AuthRequest();
        auth.setUsername("testuser");
        auth.setPassword("abc"); // too short

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(auth)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages", hasItem(containsString("password"))));
    }

    // — Security Tests —

    @Test
    @DisplayName("POST /applications without token returns 403")
    void applyWithoutToken_returns403() throws Exception {
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildValidRequest(760))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /applications without token returns 403")
    void listWithoutToken_returns403() throws Exception {
        mockMvc.perform(get("/applications"))
                .andExpect(status().isForbidden());
    }

    // — Loan Application Tests —

    @Test
    @DisplayName("Healthy application with credit score 760 is APPROVED")
    void apply_highCreditScore_approved() throws Exception {
        mockMvc.perform(post("/applications")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildValidRequest(760))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.offer.emi").isNumber())
                .andExpect(jsonPath("$.offer.interestRate").isNumber())
                .andExpect(jsonPath("$.applicationId").isNotEmpty());
    }

    @Test
    @DisplayName("Low credit score application is REJECTED")
    void apply_lowCreditScore_rejected() throws Exception {
        mockMvc.perform(post("/applications")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildValidRequest(550))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReasons").isArray())
                .andExpect(jsonPath("$.rejectionReasons", not(empty())));
    }

    @Test
    @DisplayName("Invalid loan amount returns 400 with field error")
    void apply_invalidAmount_returns400() throws Exception {
        LoanApplicationRequest req = buildValidRequest(760);
        req.getLoan().setAmount(new BigDecimal("5000")); // below 10,000 minimum

        mockMvc.perform(post("/applications")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages", hasItem(containsString("amount"))));
    }

    @Test
    @DisplayName("Missing applicant body returns 400")
    void apply_missingApplicant_returns400() throws Exception {
        mockMvc.perform(post("/applications")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loan\":{\"amount\":500000,\"tenureMonths\":36,\"purpose\":\"PERSONAL\"}}"))
                .andExpect(status().isBadRequest());
    }

    // — Audit Endpoint Tests —

    @Test
    @DisplayName("GET /applications/{id} returns the saved decision")
    void getById_returnsDecision() throws Exception {
        // Submit first, grab the ID from response
        MvcResult applyResult = mockMvc.perform(post("/applications")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildValidRequest(760))))
                .andExpect(status().isOk())
                .andReturn();

        String applicationId = objectMapper.readTree(
                applyResult.getResponse().getContentAsString()
        ).get("applicationId").asText();

        mockMvc.perform(get("/applications/" + applicationId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(applicationId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.loanAmount").isNumber());
    }

    @Test
    @DisplayName("GET /applications/{id} with unknown UUID returns 404")
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/applications/00000000-0000-0000-0000-000000000000")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /applications returns paginated list")
    void listApplications_returnsPaginatedResult() throws Exception {
        // Submit a known application first
        mockMvc.perform(post("/applications")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildValidRequest(760))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/applications?page=0&size=10")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @DisplayName("GET /applications?status=APPROVED filters correctly")
    void listApplications_filterByStatus() throws Exception {
        mockMvc.perform(get("/applications?status=APPROVED")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(equalTo("APPROVED"))));
    }

    // — Helpers —

    private LoanApplicationRequest buildValidRequest(int creditScore) {
        ApplicantRequest applicant = new ApplicantRequest();
        applicant.setName("Integration Test User");
        applicant.setAge(30);
        applicant.setMonthlyIncome(new BigDecimal("75000"));
        applicant.setEmploymentType(EmploymentType.SALARIED);
        applicant.setCreditScore(creditScore);

        LoanRequest loan = new LoanRequest();
        loan.setAmount(new BigDecimal("500000"));
        loan.setTenureMonths(36);
        loan.setPurpose(LoanPurpose.PERSONAL);

        LoanApplicationRequest req = new LoanApplicationRequest();
        req.setApplicant(applicant);
        req.setLoan(loan);
        return req;
    }
}
