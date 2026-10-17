package com.university.registration.subsystemC;

public class TranscriptValidator {
    public boolean isTranscriptReady(String studentId) {
        // demo: student id ending in "X" has a transcript flag
        return !studentId.endsWith("X");
    }
}