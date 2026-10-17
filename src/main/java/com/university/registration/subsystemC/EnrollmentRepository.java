package com.university.registration.subsystemC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.university.registration.persistence.Database;

public class EnrollmentRepository {
    private final Database db;
    public EnrollmentRepository(Database db) { this.db = db; }

    public void save(EnrollmentRecord r) {
        String sql = """
            INSERT INTO enrollment(student_id, course_id, status, amount, reason)
            VALUES(?,?,?,?,?)
            ON CONFLICT(student_id, course_id) DO UPDATE SET
              status=excluded.status, amount=excluded.amount, reason=excluded.reason""";
        try (Connection c = db.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, r.getStudentId());
            p.setString(2, r.getCourseId());
            p.setString(3, r.getStatus().name());
            p.setDouble(4, r.getAmount());
            p.setString(5, r.getReason());
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public EnrollmentRecord find(String studentId, String courseId) {
        try (Connection c = db.connect();
             PreparedStatement p = c.prepareStatement(
                 "SELECT * FROM enrollment WHERE student_id=? AND course_id=?")) {
            p.setString(1, studentId);
            p.setString(2, courseId);
            ResultSet rs = p.executeQuery();
            if (!rs.next()) return null;
            return new EnrollmentRecord(
                rs.getString("student_id"),
                rs.getString("course_id"),
                EnrollmentRecord.Status.valueOf(rs.getString("status")),
                rs.getDouble("amount"),
                rs.getString("reason"));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(String studentId, String courseId) {
        try (Connection c = db.connect();
             PreparedStatement p = c.prepareStatement(
                 "DELETE FROM enrollment WHERE student_id=? AND course_id=?")) {
            p.setString(1, studentId);
            p.setString(2, courseId);
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}