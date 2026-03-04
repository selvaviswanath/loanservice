# DEVELOPMENT_NOTES.md

## Overall Approach

The service follows a strict **layered architecture**: Controller → Service → Repository, with Domain and DTO layers as cross-cutting concerns. Each layer has a single responsibility, making the code easy to test in isolation and easy to extend.

The evaluation pipeline inside `LoanApplicationService` is deliberately linear and readable:
1. Classify risk band
2. Compute interest rate
3. Compute EMI
4. Run eligibility checks (60% threshold)
5. Run offer-gate check (50% threshold)
6. Persist to DB
7. Return structured response

---

## Key Design Decisions

### Flat JPA Entity
Instead of `@Embedded` objects (Applicant, LoanDetails, Offer), I used a single flat `LoanApplication` entity. This keeps the schema simple, the audit record self-contained, and avoids unnecessary JPA complexity for a service that currently only writes (no complex queries).

### Two EMI Thresholds (60% and 50%)
The spec defines both: a **60% eligibility rejection** (in the Eligibility Rules section) and a **50% offer acceptance gate** (in the Offer Generation section). Both are enforced in order:
- 60% check runs first via `EligibilityService` — if breached, reason is `EMI_EXCEEDS_60_PERCENT`
- 50% offer check runs only if eligibility passes — if breached, reason is `EMI_EXCEEDS_50_PERCENT_OF_INCOME`

This means any EMI between 50–60% of income hits the offer gate but not the eligibility gate — which is the correct interpretation of the spec.

### Service Decomposition
`EmiCalculatorService`, `InterestRateService`, and `EligibilityService` are all pure computation classes with no side effects and no external dependencies. This makes them trivially unit testable without any mocking.

### BigDecimal Precision Strategy
All financial math uses `scale=2, RoundingMode.HALF_UP` for final values. EMI exponentiation uses a higher internal `MathContext(15)` to prevent compound rounding errors during `(1+r)^n` — then rounds back to scale=2 only at the final step.

### Static Factory Methods on Response DTO
`LoanApplicationResponse.approved()` and `.rejected()` make the callsite in `LoanApplicationService` read like natural English. Better than a 6-argument constructor or a builder for this scale.

---

## Technology Choices

| Choice | Reason |
|---|---|
| **Spring Boot 3.4.x** | Latest stable 3.x line. Boot 4.0 just released — too early for production use (breaking changes, immature ecosystem). |
| **Java 21 LTS** | LTS support until Sept 2028. Ships Virtual Threads (Loom), Records, Pattern Matching, improved GC. Spring Boot 3.x fully supports it. |
| **H2 in-memory** | Sufficient for audit storage in this context. Zero infra overhead. Swap to PostgreSQL in one properties change. |
| **Bean Validation** | Standard Jakarta EE approach — keeps validation close to the DTO, away from business logic. |

---

## Trade-offs Considered

- **Flat entity vs embedded objects**: flat wins for simplicity; embedded would win if we needed separate query access to applicant/loan sub-objects.
- **`rejectionReasons` as comma-separated string** vs `@ElementCollection`: the former requires no join table and keeps audit records simpler to read. Trade-off: slightly harder to query by reason (acceptable for now).
- **Not returning HTTP 201** on creation: the spec shows `200 OK` implicitly through its examples. Could argue for 201 Created with `Location` header — deferred as out-of-scope.

---

## Assumptions Made

1. The spec says "reject if EMI > 60% of monthly income" (eligibility) AND "offer valid only if EMI ≤ 50%" — these are treated as two distinct, sequential gates.
2. Age boundary is inclusive: `age + tenureMonths/12.0 > 65` (strictly greater than). Exactly 65 is allowed.
3. The loan amount "10L threshold" for size premium uses strict `>` (above 10,00,000, not at or above).
4. `totalPayable = EMI × tenureMonths` (simple multiplication, as is standard for fixed-rate reducing-balance loans).
5. The `riskBand` field in the response is `null` for rejected applications (as shown in the spec's rejected example).

---

## Improvements With More Time

1. **Integration tests** using `@SpringBootTest` + `MockMvc` to test the full HTTP stack including validation error responses.
2. **`@ElementCollection` or a dedicated `RejectionReason` table** for proper queryability.
3. **GET /applications/{id}** endpoint to retrieve stored decisions for audit.
4. **Pagination on a GET /applications** endpoint for admin dashboards.
5. **Externalize business constants** (base rate, thresholds, risk premiums) to `application.properties` so they can be tuned without a code change.
6. **OpenAPI/Swagger documentation** via `springdoc-openapi`.
7. **Actuator endpoints** for health checks and metrics.
8. **Flyway/Liquibase** for schema versioning when switching to a real DB.
9. **Replace H2 with PostgreSQL** for production readiness.
