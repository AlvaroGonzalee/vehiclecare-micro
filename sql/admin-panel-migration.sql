-- Admin panel migration for an existing VehicleCare database.
-- Apply this script once against the current MySQL schema.

CREATE TABLE IF NOT EXISTS admins (
    id VARCHAR(8) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);
