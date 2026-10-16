package com.university.registration.subsystemA;

public class PrerequisiteChecker {
    public boolean satisfies(String studentId, String courseId) {
        // demo rule: student id ending in "9" fails prerequisite check
        return !studentId.endsWith("9");
    }
}