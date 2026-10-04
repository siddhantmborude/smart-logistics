-- Optional reference schema.
-- Spring JPA creates/updates tables automatically using ddl-auto=update.

CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS distances (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT NOT NULL REFERENCES locations(id),
    destination_id BIGINT NOT NULL REFERENCES locations(id),
    distance DOUBLE PRECISION NOT NULL
);
