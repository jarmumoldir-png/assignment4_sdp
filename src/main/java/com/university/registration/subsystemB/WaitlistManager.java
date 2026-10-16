package com.university.registration.subsystemB;

public class WaitlistManager {
    @SuppressWarnings("UseSpecificCatch")
    public boolean canWaitlist(String studentId) {
        try {
            return Integer.parseInt(studentId.replaceAll("\\D", "")) % 2 == 0;
        } catch (Exception e) { return false; }
    }
}