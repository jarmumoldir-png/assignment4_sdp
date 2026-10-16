package com.university.registration.subsystemA;

import com.university.registration.exception.SubsystemAFailure;

public class EligibilityService {
    private final PrerequisiteChecker prerequisites;
    private final HoldValidator holds;
    private final EligibilityRepository repository;

    public EligibilityService(PrerequisiteChecker p, HoldValidator h, EligibilityRepository r) {
        this.prerequisites = p;
        this.holds = h;
        this.repository = r;
    }

    public EligibilityRecord check(String studentId, String courseId) {
        EligibilityRecord record = new EligibilityRecord(studentId, courseId,
                EligibilityRecord.Status.NEW, null);
        repository.save(record);

        if (holds.hasFinancialHold(studentId)) {
            record.transitionTo(EligibilityRecord.Status.REJECTED, "FINANCIAL_HOLD");
            repository.save(record);
            throw new SubsystemAFailure("Financial hold on " + studentId);
        }
        if (!prerequisites.satisfies(studentId, courseId)) {
            record.transitionTo(EligibilityRecord.Status.REJECTED, "PREREQUISITE_MISSING");
            repository.save(record);
            throw new SubsystemAFailure("Prerequisites not met for " + courseId);
        }
        record.transitionTo(EligibilityRecord.Status.CHECKED, "OK");
        repository.save(record);
        return record;
    }

    public void compensate(String studentId) {
        EligibilityRecord r = repository.find(studentId);
        if (r == null) return;
        if (r.getStatus() == EligibilityRecord.Status.CHECKED) {
            r.transitionTo(EligibilityRecord.Status.COMPENSATED, "ROLLED_BACK");
            repository.save(r);
        }
    }
}