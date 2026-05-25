CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    msisdn VARCHAR(20) NOT NULL,
    step VARCHAR(50) NOT NULL,
    input TEXT,
    response_text TEXT,
    status VARCHAR(20) NOT NULL,
    error_code VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_session_id ON audit_logs(session_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);