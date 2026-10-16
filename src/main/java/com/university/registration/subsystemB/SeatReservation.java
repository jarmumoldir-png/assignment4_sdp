package com.university.registration.subsystemB;

public class SeatReservation {
    public enum Status { NEW, RESERVED, WAITLISTED, REJECTED, COMPENSATED }

    private final String studentId;
    private final String courseId;
    private Status status;
    private String reason;

    public SeatReservation(String studentId, String courseId, Status status, String reason) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.status = status;
        this.reason = reason;
    }

    public boolean canTransitionTo(Status next) {
        return switch (status) {
            case NEW -> next == Status.RESERVED || next == Status.WAITLISTED || next == Status.REJECTED;
            case RESERVED -> next == Status.COMPENSATED;
            case WAITLISTED -> next == Status.COMPENSATED;
            case REJECTED, COMPENSATED -> false;
        };
    }

    public void transitionTo(Status next, String reason) {
        if (!canTransitionTo(next))
            throw new IllegalStateException("Illegal transition " + status + " -> " + next);
        this.status = next;
        this.reason = reason;
    }

    public String getStudentId() { return studentId; }
    public String getCourseId() { return courseId; }
    public Status getStatus() { return status; }
    public String getReason() { return reason; }
}