package com.university.registration.subsystemC;

import com.university.registration.exception.SubsystemCFailure;

public class EnrollmentService {
    private final BillingCalculator billing;
    private final TranscriptValidator transcript;
    private final EnrollmentRepository repository;

    public EnrollmentService(BillingCalculator b, TranscriptValidator t, EnrollmentRepository r) {
        this.billing = b; this.transcript = t; this.repository = r;
    }

    public EnrollmentRecord confirm(String studentId, String courseId) {
        EnrollmentRecord r = new EnrollmentRecord(studentId, courseId,
                EnrollmentRecord.Status.NEW, 0.0, null);
        repository.save(r);

        if (!transcript.isTranscriptReady(studentId)) {
            r.transitionTo(EnrollmentRecord.Status.REJECTED, "TRANSCRIPT_NOT_READY");
            repository.save(r);
            throw new SubsystemCFailure("Transcript not ready for " + studentId);
        }
        if (!billing.canCharge(studentId)) {
            r.transitionTo(EnrollmentRecord.Status.REJECTED, "BILLING_REJECTED");
            repository.save(r);
            throw new SubsystemCFailure("Billing rejected for " + studentId);
        }
        double amount = billing.compute(studentId, courseId);
        r.setAmount(amount);
        r.transitionTo(EnrollmentRecord.Status.BILLED, "BILLED");
        repository.save(r);
        r.transitionTo(EnrollmentRecord.Status.CONFIRMED, "OK");
        repository.save(r);
        return r;
    }

    public void compensate(String studentId, String courseId) {
        EnrollmentRecord r = repository.find(studentId, courseId);
        if (r == null) return;
        if (r.getStatus() == EnrollmentRecord.Status.BILLED
                || r.getStatus() == EnrollmentRecord.Status.CONFIRMED) {
            r.transitionTo(EnrollmentRecord.Status.COMPENSATED, "ROLLED_BACK");
            repository.save(r);
        }
    }
}