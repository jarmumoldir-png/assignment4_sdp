package com.university.registration.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DEFAULT_URL = "jdbc:sqlite:data/registration.db";
    private final String url;

    public Database() { this(DEFAULT_URL); }
    public Database(String url) { this.url = url; }

    public Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        c.setAutoCommit(true);
        return c;
    }

    public void initialize() {
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS eligibility (
                    student_id TEXT PRIMARY KEY,
                    course_id TEXT NOT NULL,
                    status TEXT NOT NULL,
                    reason TEXT
                )""");
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS seat_reservation (
                    student_id TEXT NOT NULL,
                    course_id TEXT NOT NULL,
                    status TEXT NOT NULL,
                    reason TEXT,
                    PRIMARY KEY (student_id, course_id)
                )""");
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS enrollment (
                    student_id TEXT NOT NULL,
                    course_id TEXT NOT NULL,
                    status TEXT NOT NULL,
                    amount REAL,
                    reason TEXT,
                    PRIMARY KEY (student_id, course_id)
                )""");
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS chain_progress (
                    student_id TEXT NOT NULL,
                    course_id TEXT NOT NULL,
                    status TEXT NOT NULL,
                    updated_at TEXT NOT NULL,
                    PRIMARY KEY (student_id, course_id)
                )""");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DB", e);
        }
    }
}