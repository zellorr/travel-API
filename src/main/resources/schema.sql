DROP VIEW IF EXISTS v_packages_summary;
DROP VIEW IF EXISTS v_all_bookings;
DROP TABLE IF EXISTS package_bookings;
DROP TABLE IF EXISTS travel_packages;
DROP TABLE IF EXISTS flight_bookings;
DROP TABLE IF EXISTS hotel_bookings;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(120) NOT NULL,
                           email VARCHAR(180) NOT NULL UNIQUE,
                           phone VARCHAR(20) NOT NULL,
                           passport_number VARCHAR(20) NOT NULL,
                           created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT chk_customers_name_not_empty CHECK (TRIM(name) <> ''),
                           CONSTRAINT chk_customers_email_valid CHECK (
                               email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
)
    );

CREATE TABLE bookings (
                          id BIGSERIAL PRIMARY KEY,
                          customer_id BIGINT NOT NULL,
                          booking_date DATE NOT NULL,
                          total_price NUMERIC(12,2) NOT NULL CHECK (total_price >= 0),
                          status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
                          type VARCHAR(30) NOT NULL CHECK (type IN ('FLIGHT', 'HOTEL')),
                          created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_bookings_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT
);

CREATE TABLE flight_bookings (
                                 booking_id BIGINT PRIMARY KEY,
                                 flight_number VARCHAR(20) NOT NULL,
                                 origin VARCHAR(100) NOT NULL,
                                 destination VARCHAR(100) NOT NULL,
                                 seat_class VARCHAR(30) NOT NULL CHECK (seat_class IN ('ECONOMY', 'BUSINESS', 'FIRST_CLASS')),
                                 CONSTRAINT fk_flight_bookings_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE TABLE hotel_bookings (
                                booking_id BIGINT PRIMARY KEY,
                                hotel_name VARCHAR(150) NOT NULL,
                                room_type VARCHAR(50) NOT NULL CHECK (room_type IN ('STANDARD', 'DELUXE', 'SUITE', 'PRESIDENTIAL')),
                                nights INTEGER NOT NULL CHECK (nights >= 1 AND nights <= 365),
                                CONSTRAINT fk_hotel_bookings_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE TABLE travel_packages (
                                 id BIGSERIAL PRIMARY KEY,
                                 name VARCHAR(150) NOT NULL,
                                 customer_id BIGINT NOT NULL,
                                 discount_percentage NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (discount_percentage >= 0 AND discount_percentage <= 100),
                                 created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_travel_packages_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
                                 CONSTRAINT chk_travel_packages_name_not_empty CHECK (TRIM(name) <> '')
);

CREATE TABLE package_bookings (
                                  package_id BIGINT NOT NULL,
                                  booking_id BIGINT NOT NULL,
                                  added_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (package_id, booking_id),
                                  CONSTRAINT fk_package_bookings_package FOREIGN KEY (package_id) REFERENCES travel_packages(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_package_bookings_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE INDEX idx_bookings_customer ON bookings(customer_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_date ON bookings(booking_date);
CREATE INDEX idx_bookings_type ON bookings(type);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_flight_bookings_flight_number ON flight_bookings(flight_number);
CREATE INDEX idx_hotel_bookings_hotel_name ON hotel_bookings(hotel_name);

CREATE VIEW v_all_bookings AS
SELECT
    b.id,
    b.customer_id,
    b.booking_date,
    b.total_price,
    b.status,
    b.type,
    b.created_at,
    c.name AS customer_name,
    c.email AS customer_email,
    c.phone AS customer_phone,
    fb.flight_number,
    fb.origin,
    fb.destination,
    fb.seat_class,
    hb.hotel_name,
    hb.room_type,
    hb.nights
FROM bookings b
         JOIN customers c ON c.id = b.customer_id
         LEFT JOIN flight_bookings fb ON fb.booking_id = b.id
         LEFT JOIN hotel_bookings hb ON hb.booking_id = b.id;

CREATE VIEW v_packages_summary AS
SELECT
    p.id,
    p.name,
    p.customer_id,
    c.name AS customer_name,
    c.email AS customer_email,
    p.discount_percentage,
    p.created_at,
    COUNT(pb.booking_id) AS booking_count,
    COALESCE(SUM(b.total_price), 0) AS total_before_discount,
    ROUND(COALESCE(SUM(b.total_price), 0) * (1 - p.discount_percentage / 100.0), 2) AS total_after_discount,
    ROUND(COALESCE(SUM(b.total_price), 0) * (p.discount_percentage / 100.0), 2) AS discount_amount
FROM travel_packages p
         JOIN customers c ON c.id = p.customer_id
         LEFT JOIN package_bookings pb ON pb.package_id = p.id
         LEFT JOIN bookings b ON b.id = pb.booking_id
GROUP BY
    p.id,
    p.name,
    p.customer_id,
    c.name,
    c.email,
    p.discount_percentage,
    p.created_at;

INSERT INTO customers (name, email, phone, passport_number) VALUES
                                                                ('Bob Chen', 'bob.chen@email.com', '+86-555-2002', 'CN777888999'),
                                                                ('Updated Name', 'updated.email@test.com', '+1-555-9999', 'US555888999'),
                                                                ('Alice Williams', 'alice.w@email.com', '+1-555-1001', 'US555888999'),
                                                                ('Emma Johnson', 'emma.j@email.com', '+1-555-0102', 'US987654321'),
                                                                ('Ahmed Hassan', 'ahmed.h@email.com', '+971-555-0103', 'AE555666777');

INSERT INTO bookings (customer_id, booking_date, total_price, status, type) VALUES
                                                                                (1, '2026-02-22', 750.00, 'PENDING', 'FLIGHT'),
                                                                                (3, '2026-02-23', 750.00, 'PENDING', 'FLIGHT'),
                                                                                (4, '2026-03-15', 450.00, 'CONFIRMED', 'FLIGHT'),
                                                                                (5, '2026-04-20', 850.00, 'PENDING', 'FLIGHT');

INSERT INTO flight_bookings (booking_id, flight_number, origin, destination, seat_class) VALUES
                                                                                             (1, 'AA123', 'New York', 'Tokyo', 'BUSINESS'),
                                                                                             (2, 'AA123', 'New York', 'Tokyo', 'BUSINESS'),
                                                                                             (3, 'EK205', 'Los Angeles', 'Dubai', 'ECONOMY'),
                                                                                             (4, 'BA401', 'New York JFK', 'London Heathrow', 'BUSINESS');

INSERT INTO bookings (customer_id, booking_date, total_price, status, type) VALUES
                                                                                (1, '2026-02-23', 800.00, 'PENDING', 'HOTEL'),
                                                                                (3, '2026-02-24', 800.00, 'PENDING', 'HOTEL'),
                                                                                (2, '2026-04-10', 1200.00, 'CONFIRMED', 'HOTEL'),
                                                                                (4, '2026-03-16', 400.00, 'CONFIRMED', 'HOTEL');

INSERT INTO hotel_bookings (booking_id, hotel_name, room_type, nights) VALUES
                                                                           (5, 'Grand Hyatt Tokyo', 'DELUXE', 4),
                                                                           (6, 'Grand Hyatt Tokyo', 'DELUXE', 4),
                                                                           (7, 'Le Grand Paris Hotel', 'SUITE', 3),
                                                                           (8, 'London City Inn', 'STANDARD', 4);

INSERT INTO travel_packages (name, customer_id, discount_percentage) VALUES
                                                                         ('Tokyo Adventure Package', 1, 10.00),
                                                                         ('European Tour', 2, 15.00);

INSERT INTO package_bookings (package_id, booking_id) VALUES
                                                          (1, 1),
                                                          (1, 5),
                                                          (2, 7);
