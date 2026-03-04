-- V1: Initial schema — creates the two core tables

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Loan applications — stores every evaluation decision for audit
CREATE TABLE IF NOT EXISTS loan_applications (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    applicant_name   VARCHAR(255) NOT NULL,
    applicant_age    INT          NOT NULL,
    monthly_income   NUMERIC(15,2) NOT NULL,
    employment_type  VARCHAR(20)  NOT NULL,
    credit_score     INT          NOT NULL,
    loan_amount      NUMERIC(15,2) NOT NULL,
    tenure_months    INT          NOT NULL,
    loan_purpose     VARCHAR(20)  NOT NULL,
    status           VARCHAR(10)  NOT NULL,
    risk_band        VARCHAR(10),
    interest_rate    NUMERIC(5,2),
    emi              NUMERIC(15,2),
    total_payable    NUMERIC(15,2),
    rejection_reasons VARCHAR(500),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Index for quick audit queries by status and date
CREATE INDEX IF NOT EXISTS idx_loan_applications_status ON loan_applications(status);
CREATE INDEX IF NOT EXISTS idx_loan_applications_created_at ON loan_applications(created_at);
