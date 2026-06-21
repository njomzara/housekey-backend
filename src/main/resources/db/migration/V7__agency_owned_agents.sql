CREATE SEQUENCE agents_id_seq START WITH 100 INCREMENT BY 1;

ALTER TABLE agents ADD COLUMN agency_user_id BIGINT;
ALTER TABLE agents ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE agents ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE agents ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE agents
    ADD CONSTRAINT fk_agents_agency_user
    FOREIGN KEY (agency_user_id) REFERENCES users(id);

CREATE INDEX idx_agents_agency_active ON agents(agency_user_id, active);

UPDATE users
SET role = 'AGENCY'
WHERE role IN ('BUYER', 'AGENT');

ALTER TABLE users DROP CONSTRAINT ck_users_role;

ALTER TABLE users
    ADD CONSTRAINT ck_users_role CHECK (role IN ('AGENCY', 'ADMIN'));
