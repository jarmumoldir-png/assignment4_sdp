package com.university.registration.subsystemA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.university.registration.persistence.Database;

public class EligibilityRepository {
    private final Database db;
    public EligibilityRepository(Database db) { this.db = db; }

    public void save(EligibilityRecord r) {
        String sql = """
            INSERT INTO eligibility(student_id, course_id, status, reason)
            VALUES(?,?,?,?)
            ON CONFLICT(student_id) DO UPDATE SET
              course_id=excluded.course_id,
              status=excluded.status,
              reason=excluded.reason""";
        try (Connection c = db.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, r.getStudentId());
            p.setString(2, r.getCourseId());
            p.setString(3, r.getStatus().name());
            p.setString(4, r.getReason());
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public EligibilityRecord find(String studentId) {
        try (Connection c = db.connect();
             PreparedStatement p = c.prepareStatement("SELECT * FROM eligibility WHERE student_id=?")) {
            p.setString(1, studentId);
            ResultSet rs = p.executeQuery();
            if (!rs.next()) return null;
            return new EligibilityRecord(
                rs.getString("student_id"),
                rs.getString("course_id"),
                EligibilityRecord.Status.valueOf(rs.getString("status")),
                rs.getString("reason"));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(String studentId) {
        try (Connection c = db.connect();
             PreparedStatement p = c.prepareStatement("DELETE FROM eligibility WHERE student_id=?")) {
            p.setString(1, studentId);
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}