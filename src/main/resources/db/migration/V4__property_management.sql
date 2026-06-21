CREATE SEQUENCE property_listing_id_seq START WITH 100 INCREMENT BY 1;

ALTER TABLE property_listing ADD COLUMN owner_user_id BIGINT;
ALTER TABLE property_listing ADD COLUMN agent_user_id BIGINT;
ALTER TABLE property_listing ADD COLUMN lifecycle_status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED';
ALTER TABLE property_listing ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE property_listing ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE property_listing ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE property_listing ALTER COLUMN published_at DROP NOT NULL;

UPDATE property_listing
SET created_at = COALESCE(published_at, last_update_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(last_update_at, published_at, CURRENT_TIMESTAMP),
    lifecycle_status = 'PUBLISHED'
WHERE lifecycle_status = 'PUBLISHED';

ALTER TABLE property_listing
    ADD CONSTRAINT fk_property_listing_owner_user
    FOREIGN KEY (owner_user_id) REFERENCES users(id);

ALTER TABLE property_listing
    ADD CONSTRAINT fk_property_listing_agent_user
    FOREIGN KEY (agent_user_id) REFERENCES users(id);

ALTER TABLE property_listing
    ADD CONSTRAINT ck_property_listing_lifecycle_status
    CHECK (lifecycle_status IN ('DRAFT', 'PENDING_REVIEW', 'PUBLISHED', 'ARCHIVED'));

CREATE INDEX idx_property_listing_owner_status ON property_listing(owner_user_id, lifecycle_status);
CREATE INDEX idx_property_listing_agent_user ON property_listing(agent_user_id);
CREATE INDEX idx_property_listing_lifecycle_published ON property_listing(lifecycle_status, published_at);
CREATE INDEX idx_property_listing_lifecycle_city_type ON property_listing(lifecycle_status, city, property_type);
CREATE INDEX idx_property_listing_updated_at ON property_listing(updated_at);
