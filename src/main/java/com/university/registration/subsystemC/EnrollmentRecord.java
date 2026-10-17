package com.university.registration.subsystemC;

public class EnrollmentRecord {
    public enum Status { NEW, BILLED, CONFIRMED, REJECTED, COMPENSATED }

    private final String studentId;
    private final String courseId;
    private Status status;
    private double amount;
    private String reason;

    public EnrollmentRecord(String studentId, String courseId, Status status, double amount, String reason) {
        this.studentId = studentId; this.courseId = courseId;
        this.status = status; this.amount = amount; this.reason = reason;
    }

    public boolean canTransitionTo(Status next) {
        return switch (status) {
            case NEW -> next == Status.BILLED || next == Status.REJECTED;
            case BILLED -> next == Status.CONFIRMED || next == Status.COMPENSATED;
            case CONFIRMED -> next == Status.COMPENSATED;
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
    public double getAmount() { return amount; }
    public void setAmount(double a) { this.amount = a; }
    public String getReason() { return reason; }
}