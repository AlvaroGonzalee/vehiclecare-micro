-- VehicleCare backend schema reference
-- Database flavor: MySQL 8+
-- This file documents the current schema used by the backend.

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(8) PRIMARY KEY,
    name VARCHAR(10) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admins (
    id VARCHAR(8) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS brands (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    logo_url VARCHAR(255),
    active BIT(1) NOT NULL
);

CREATE TABLE IF NOT EXISTS models (
    id VARCHAR(10) PRIMARY KEY,
    brand_id VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    active BIT(1) NOT NULL,
    CONSTRAINT fk_models_brand
        FOREIGN KEY (brand_id) REFERENCES brands(id)
);

CREATE INDEX idx_models_brand_id ON models(brand_id);

CREATE TABLE IF NOT EXISTS vehicles (
    id VARCHAR(8) PRIMARY KEY,
    user_id VARCHAR(8) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    vehicle_year INT NOT NULL,
    license_plate VARCHAR(255) NOT NULL UNIQUE,
    vin VARCHAR(255) UNIQUE,
    current_kilometers INT,
    fuel_type VARCHAR(255),
    image_url VARCHAR(500),
    CONSTRAINT fk_vehicles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_vehicles_user_id ON vehicles(user_id);

CREATE TABLE IF NOT EXISTS maintenance_records (
    id VARCHAR(8) PRIMARY KEY,
    vehicle_id VARCHAR(8) NOT NULL,
    title VARCHAR(255) NOT NULL,
    maintenance_date DATE NOT NULL,
    category VARCHAR(255) NOT NULL,
    kilometers INT,
    price DECIMAL(38,2),
    description VARCHAR(255),
    CONSTRAINT fk_maintenance_records_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

CREATE INDEX idx_maintenance_records_vehicle_id ON maintenance_records(vehicle_id);

CREATE TABLE IF NOT EXISTS attachments (
    id VARCHAR(8) PRIMARY KEY,
    maintenance_record_id VARCHAR(8) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(120) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    CONSTRAINT fk_attachments_maintenance_record
        FOREIGN KEY (maintenance_record_id) REFERENCES maintenance_records(id) ON DELETE CASCADE
);

CREATE INDEX idx_attachments_maintenance_record_id ON attachments(maintenance_record_id);
