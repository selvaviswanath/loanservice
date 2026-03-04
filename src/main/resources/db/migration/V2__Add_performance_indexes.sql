-- V2: Add indexes for common auth and reporting queries

-- Fast username lookup on every login/register
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Useful for admin dashboards filtering by employment type or credit score range
CREATE INDEX IF NOT EXISTS idx_loan_applications_employment_type ON loan_applications(employment_type);
CREATE INDEX IF NOT EXISTS idx_loan_applications_credit_score ON loan_applications(credit_score);
