CREATE TABLE agents (
    id BIGINT PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    organization VARCHAR(160),
    email VARCHAR(160),
    phone VARCHAR(80),
    image VARCHAR(255)
);

CREATE TABLE property_listing (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    property_type VARCHAR(80) NOT NULL,
    city VARCHAR(120) NOT NULL,
    zip_code VARCHAR(40) NOT NULL,
    latitude NUMERIC(10, 6) NOT NULL,
    longitude NUMERIC(10, 6) NOT NULL,
    formatted_address VARCHAR(500) NOT NULL,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    price_dollar_sale BIGINT,
    price_dollar_rent BIGINT,
    price_euro_sale BIGINT,
    price_euro_rent BIGINT,
    price_dinars_sale BIGINT,
    price_dinars_rent BIGINT,
    bedrooms INTEGER NOT NULL,
    bathrooms INTEGER NOT NULL,
    garages INTEGER NOT NULL,
    area_value INTEGER NOT NULL,
    area_unit VARCHAR(40) NOT NULL,
    year_built INTEGER NOT NULL,
    ratings_count INTEGER NOT NULL,
    ratings_value INTEGER NOT NULL,
    published_at TIMESTAMP NOT NULL,
    last_update_at TIMESTAMP NOT NULL,
    views INTEGER NOT NULL DEFAULT 0,
    agent_id BIGINT,
    CONSTRAINT fk_property_listing_agent FOREIGN KEY (agent_id) REFERENCES agents(id)
);

CREATE TABLE listing_status (
    property_listing_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL,
    name VARCHAR(80) NOT NULL,
    PRIMARY KEY (property_listing_id, sort_order),
    CONSTRAINT fk_listing_status_property FOREIGN KEY (property_listing_id) REFERENCES property_listing(id) ON DELETE CASCADE
);

CREATE TABLE listing_neighborhood (
    property_listing_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL,
    name VARCHAR(120) NOT NULL,
    PRIMARY KEY (property_listing_id, sort_order),
    CONSTRAINT fk_listing_neighborhood_property FOREIGN KEY (property_listing_id) REFERENCES property_listing(id) ON DELETE CASCADE
);

CREATE TABLE listing_street (
    property_listing_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL,
    name VARCHAR(160) NOT NULL,
    PRIMARY KEY (property_listing_id, sort_order),
    CONSTRAINT fk_listing_street_property FOREIGN KEY (property_listing_id) REFERENCES property_listing(id) ON DELETE CASCADE
);

CREATE TABLE listing_feature (
    property_listing_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL,
    name VARCHAR(120) NOT NULL,
    PRIMARY KEY (property_listing_id, sort_order),
    CONSTRAINT fk_listing_feature_property FOREIGN KEY (property_listing_id) REFERENCES property_listing(id) ON DELETE CASCADE
);

CREATE TABLE listing_additional_feature (
    property_listing_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL,
    name VARCHAR(120) NOT NULL,
    feature_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (property_listing_id, sort_order),
    CONSTRAINT fk_listing_additional_feature_property FOREIGN KEY (property_listing_id) REFERENCES property_listing(id) ON DELETE CASCADE
);

CREATE TABLE listing_gallery_image (
    property_listing_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL,
    small_url VARCHAR(255) NOT NULL,
    medium_url VARCHAR(255) NOT NULL,
    big_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (property_listing_id, sort_order),
    CONSTRAINT fk_listing_gallery_image_property FOREIGN KEY (property_listing_id) REFERENCES property_listing(id) ON DELETE CASCADE
);

CREATE TABLE listing_floor_plan (
    property_listing_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL,
    name VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    area_value INTEGER NOT NULL,
    area_unit VARCHAR(40) NOT NULL,
    rooms INTEGER NOT NULL,
    baths INTEGER NOT NULL,
    image VARCHAR(255) NOT NULL,
    PRIMARY KEY (property_listing_id, sort_order),
    CONSTRAINT fk_listing_floor_plan_property FOREIGN KEY (property_listing_id) REFERENCES property_listing(id) ON DELETE CASCADE
);

CREATE TABLE listing_video (
    property_listing_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL,
    name VARCHAR(160) NOT NULL,
    link VARCHAR(500) NOT NULL,
    PRIMARY KEY (property_listing_id, sort_order),
    CONSTRAINT fk_listing_video_property FOREIGN KEY (property_listing_id) REFERENCES property_listing(id) ON DELETE CASCADE
);

CREATE INDEX idx_property_listing_city_type ON property_listing(city, property_type);
CREATE INDEX idx_property_listing_price_usd_sale ON property_listing(price_dollar_sale);
CREATE INDEX idx_property_listing_price_usd_rent ON property_listing(price_dollar_rent);
CREATE INDEX idx_listing_feature_name ON listing_feature(name);
