package com.university.registration.facade;

import com.university.registration.subsystemA.*;
import com.university.registration.subsystemB.*;
import com.university.registration.subsystemC.*;

public class AdminFacade {
    private final EligibilityRepository aRepo;
    private final SeatRepository bRepo;
    private final EnrollmentRepository cRepo;
    private final RegistrationFacade registration;
    private final SeatService seats;
    private final EligibilityService eligibility;
    private final EnrollmentService enrollment;

    public AdminFacade(EligibilityService a, SeatService b, EnrollmentService c,
                       EligibilityRepository aRepo, SeatRepository bRepo,
                       EnrollmentRepository cRepo, RegistrationFacade r) {
        this.eligibility = a; this.seats = b; this.enrollment = c;
        this.aRepo = aRepo; this.bRepo = bRepo; this.cRepo = cRepo;
        this.registration = r;
    }

    public String inspect(String studentId, String courseId) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chain: ").append(registration.readChain(studentId, courseId)).append("\n");
        EligibilityRecord a = aRepo.find(studentId);
        if (a != null) sb.append("A: ").append(a.getStatus()).append(" (").append(a.getReason()).append(")\n");
        SeatReservation b = bRepo.find(studentId, courseId);
        if (b != null) sb.append("B: ").append(b.getStatus()).append(" (").append(b.getReason()).append(")\n");
        EnrollmentRecord c = cRepo.find(studentId, courseId);
        if (c != null) sb.append("C: ").append(c.getStatus())
                .append(" amount=").append(c.getAmount())
                .append(" (").append(c.getReason()).append(")\n");
        return sb.toString();
    }

    public void forceCompensate(String studentId, String courseId) {
        ChainStatus s = registration.readChain(studentId, courseId);
        if (s == null) return;
        switch (s) {
            case PENDING_C -> { seats.compensate(studentId, courseId); eligibility.compensate(studentId); }
            case PENDING_B -> eligibility.compensate(studentId);
            case COMPLETED -> {
                enrollment.compensate(studentId, courseId);
                seats.compensate(studentId, courseId);
                eligibility.compensate(studentId);
            }
            default -> { }
        }
    }
}