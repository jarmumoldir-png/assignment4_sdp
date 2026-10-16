package com.university.registration.subsystemB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.university.registration.persistence.Database;

public class SeatRepository {
    private final Database db;
    public SeatRepository(Database db) { this.db = db; }

    public void save(SeatReservation r) {
        String sql = """
            INSERT INTO seat_reservation(student_id, course_id, status, reason)
            VALUES(?,?,?,?)
            ON CONFLICT(student_id, course_id) DO UPDATE SET
              status=excluded.status, reason=excluded.reason""";
        try (Connection c = db.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, r.getStudentId());
            p.setString(2, r.getCourseId());
            p.setString(3, r.getStatus().name());
            p.setString(4, r.getReason());
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public SeatReservation find(String studentId, String courseId) {
        try (Connection c = db.connect();
             PreparedStatement p = c.prepareStatement(
                 "SELECT * FROM seat_reservation WHERE student_id=? AND course_id=?")) {
            p.setString(1, studentId);
            p.setString(2, courseId);
            ResultSet rs = p.executeQuery();
            if (!rs.next()) return null;
            return new SeatReservation(
                rs.getString("student_id"),
                rs.getString("course_id"),
                SeatReservation.Status.valueOf(rs.getString("status")),
                rs.getString("reason"));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(String studentId, String courseId) {
        try (Connection c = db.connect();
             PreparedStatement p = c.prepareStatement(
                 "DELETE FROM seat_reservation WHERE student_id=? AND course_id=?")) {
            p.setString(1, studentId);
            p.setString(2, courseId);
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}