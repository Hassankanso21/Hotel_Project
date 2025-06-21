-- Create the database
CREATE DATABASE IF NOT EXISTS hotel_db;
USE hotel_db;

-- Drop tables if they exist
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS hotel_room;
DROP TABLE IF EXISTS user_info;

-- Create hotel_room table
CREATE TABLE hotel_room (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            room_number VARCHAR(10) NOT NULL,
                            category VARCHAR(20) NOT NULL,
                            available BOOLEAN NOT NULL,
                            price_per_night DOUBLE NOT NULL
);

-- Create reservation table
CREATE TABLE reservation (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             customer_name VARCHAR(100) NOT NULL,
                             room_id BIGINT NOT NULL,
                             check_in_date DATE NOT NULL,
                             check_out_date DATE NOT NULL,
                             payment_status BOOLEAN NOT NULL,
                             CONSTRAINT fk_reservation_room
                                 FOREIGN KEY (room_id)
                                     REFERENCES hotel_room(id)
                                     ON DELETE RESTRICT
                                     ON UPDATE CASCADE
);

-- Create user_info table
CREATE TABLE user_info (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           email VARCHAR(100) NOT NULL UNIQUE,
                           password VARCHAR(255) NOT NULL,
                           roles VARCHAR(50) NOT NULL
);
