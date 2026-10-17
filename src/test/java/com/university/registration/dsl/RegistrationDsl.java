package com.university.registration.dsl;

import com.university.registration.facade.*;
import com.university.registration.persistence.Database;
import com.university.registration.subsystemA.*;
import com.university.registration.subsystemB.*;
import com.university.registration.subsystemC.*;

public class RegistrationDsl {
    public final Database db;
    public final RegistrationFacade registration;
    public final AdminFacade admin;
    public final EligibilityRepository aRepo;
    public final SeatRepository bRepo;
    public final EnrollmentRepository cRepo;
    public final SeatAllocator allocator;

    public RegistrationDsl(String dbUrl, int capacity) {
        db = new Database(dbUrl);
        db.initialize();

        aRepo = new EligibilityRepository(db);
        EligibilityService a = new EligibilityService(new PrerequisiteChecker(), new HoldValidator(), aRepo);
        bRepo = new SeatRepository(db);
        allocator = new SeatAllocator(capacity);
        SeatService b = new SeatService(allocator, new WaitlistManager(), bRepo);
        cRepo = new EnrollmentRepository(db);
        EnrollmentService c = new EnrollmentService(new BillingCalculator(), new TranscriptValidator(), cRepo);

        registration = new RegistrationFacade(a, b, c, db);
        admin = new AdminFacade(a, b, c, aRepo, bRepo, cRepo, registration);
    }

    public RegistrationDsl givenStudentWantsCourse(String studentId, String courseId) {
        return this;
    }

    public void whenStudentEnrolls(String studentId, String courseId) {
        registration.enroll(studentId, courseId);
    }

    public void whenStudentEnrollsExpectingFailure(String studentId, String courseId) {
        try {
            registration.enroll(studentId, courseId);
            throw new AssertionError("Expected chain failure but none occurred");
        } catch (com.university.registration.exception.ChainException expected) { }
    }

    public void thenEligibilityIs(String studentId, EligibilityRecord.Status expected) {
        EligibilityRecord r = aRepo.find(studentId);
        if (r == null || r.getStatus() != expected)
            throw new AssertionError("Expected eligibility " + expected + " but was " + (r == null ? "null" : r.getStatus()));
    }

    public void thenSeatIs(String studentId, String courseId, SeatReservation.Status expected) {
        SeatReservation r = bRepo.find(studentId, courseId);
        if (r == null || r.getStatus() != expected)
            throw new AssertionError("Expected seat " + expected + " but was " + (r == null ? "null" : r.getStatus()));
    }

    public void thenEnrollmentIs(String studentId, String courseId, EnrollmentRecord.Status expected) {
        EnrollmentRecord r = cRepo.find(studentId, courseId);
        if (r == null || r.getStatus() != expected)
            throw new AssertionError("Expected enrollment " + expected + " but was " + (r == null ? "null" : r.getStatus()));
    }

    public void thenChainIs(String studentId, String courseId, ChainStatus expected) {
        ChainStatus s = registration.readChain(studentId, courseId);
        if (s != expected)
            throw new AssertionError("Expected chain " + expected + " but was " + s);
    }

    public void cleanup(String studentId, String courseId) {
        aRepo.delete(studentId);
        bRepo.delete(studentId, courseId);
        cRepo.delete(studentId, courseId);
    }
}