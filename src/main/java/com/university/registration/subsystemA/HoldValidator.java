package com.university.registration.subsystemA;

public class HoldValidator {
    public boolean hasFinancialHold(String studentId) {
        // demo rule: student id starting with "H" has a financial hold
        return studentId.startsWith("H");
    }
}