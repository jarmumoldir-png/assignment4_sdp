package com.university.registration.facade;

import com.university.registration.exception.*;
import com.university.registration.persistence.Database;
import com.university.registration.subsystemA.*;
import com.university.registration.subsystemB.*;
import com.university.registration.subsystemC.*;

import java.sql.*;
import java.time.Instant;

public class RegistrationFacade {
    private final EligibilityService eligibility;
    private final SeatService seats;
    private final EnrollmentService enrollment;
    private final Database db;

    public RegistrationFacade(EligibilityService a, SeatService b,
                              EnrollmentService c, Database db) {
        this.eligibility = a; this.seats = b; this.enrollment = c; this.db = db;
    }

    /** The one logical operation: enroll a student in a course. */
    public void enroll(String studentId, String courseId) {
        // 1. Run A
        try {
            eligibility.check(studentId, courseId);
            persistChain(studentId, courseId, ChainStatus.PENDING_B);
        } catch (SubsystemAFailure e) {
            persistChain(studentId, courseId, ChainStatus.FAILED_AT_A);
            throw e;
        }

        // 2. Run B
        try {
            seats.allocate(studentId, courseId);
            persistChain(studentId, courseId, ChainStatus.PENDING_C);
        } catch (SubsystemBFailure e) {
            // B failed after A committed → compensate A only
            eligibility.compensate(studentId);
            persistChain(studentId, courseId, ChainStatus.FAILED_AT_B);
            throw e;
        }

        // 3. Run C
        try {
            enrollment.confirm(studentId, courseId);
            persistChain(studentId, courseId, ChainStatus.COMPLETED);
        } catch (SubsystemCFailure e) {
            // C failed after A + B committed → compensate B then A (in that order)
            seats.compensate(studentId, courseId);
            eligibility.compensate(studentId);
            persistChain(studentId, courseId, ChainStatus.FAILED_AT_C);
            throw e;
        }
    }

    /** Called at startup: reads persisted status and resumes/compensates. */
    public void resume(String studentId, String courseId) {
        ChainStatus s = readChain(studentId, courseId);
        if (s == null) return;
        switch (s) {
            case PENDING_B -> {
                // A committed but B never did → compensate A
                eligibility.compensate(studentId);
                persistChain(studentId, courseId, ChainStatus.COMPENSATED);
            }
            case PENDING_C -> {
                // A + B committed but C never did → compensate B, A
                seats.compensate(studentId, courseId);
                eligibility.compensate(studentId);
                persistChain(studentId, courseId, ChainStatus.COMPENSATED);
            }
            default -> {}
        }
    }

    private void persistChain(String studentId, String courseId, ChainStatus status) {
        try (Connection c = db.connect();
             PreparedStatement p = c.prepareStatement("""
                INSERT INTO chain_progress(student_id, course_id, status, updated_at)
                VALUES(?,?,?,?)
                ON CONFLICT(student_id, course_id) DO UPDATE SET
                  status=excluded.status, updated_at=excluded.updated_at""")) {
            p.setString(1, studentId);
            p.setString(2, courseId);
            p.setString(3, status.name());
            p.setString(4, Instant.now().toString());
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public ChainStatus readChain(String studentId, String courseId) {
        try (Connection c = db.connect();
             PreparedStatement p = c.prepareStatement(
                "SELECT status FROM chain_progress WHERE student_id=? AND course_id=?")) {
            p.setString(1, studentId);
            p.setString(2, courseId);
            ResultSet rs = p.executeQuery();
            return rs.next() ? ChainStatus.valueOf(rs.getString("status")) : null;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}