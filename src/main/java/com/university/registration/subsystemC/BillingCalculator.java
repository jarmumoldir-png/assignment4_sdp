package com.university.registration.subsystemC;

public class BillingCalculator {
    public double compute(String studentId, String courseId) {
        double base = 500.0;
        if (courseId.startsWith("LAB")) base += 150.0;
        return base;
    }

    public boolean canCharge(String studentId) {
        // demo rule: student id starting with "B" is blocked
        return !studentId.startsWith("B");
    }
}