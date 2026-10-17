package com.university.registration;

import com.university.registration.dsl.RegistrationDsl;
import com.university.registration.facade.ChainStatus;
import com.university.registration.subsystemA.EligibilityRecord;
import com.university.registration.subsystemB.SeatReservation;
import com.university.registration.subsystemC.EnrollmentRecord;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.UUID;

class RegistrationFacadeTest {

    private RegistrationDsl dsl;
    private String dbUrl;

    @BeforeEach
    void setUp() {
        dbUrl = "jdbc:sqlite:target/test-" + UUID.randomUUID() + ".db";
        dsl = new RegistrationDsl(dbUrl, 2);
    }

    @AfterEach
    void tearDown() {
        new File(dbUrl.replace("jdbc:sqlite:", "")).delete();
    }

    @Test
    void happyPath_completesAllThreeSubsystems() {
        dsl.givenStudentWantsCourse("S1", "SE101");
        dsl.whenStudentEnrolls("S1", "SE101");
        dsl.thenEligibilityIs("S1", EligibilityRecord.Status.CHECKED);
        dsl.thenSeatIs("S1", "SE101", SeatReservation.Status.RESERVED);
        dsl.thenEnrollmentIs("S1", "SE101", EnrollmentRecord.Status.CONFIRMED);
        dsl.thenChainIs("S1", "SE101", ChainStatus.COMPLETED);
    }

    @Test
    void bFailsAfterA_compensatesAOnly() {
        dsl = new RegistrationDsl(dbUrl, 0);
        dsl.givenStudentWantsCourse("S1", "SE101");
        dsl.whenStudentEnrollsExpectingFailure("S1", "SE101");
        dsl.thenEligibilityIs("S1", EligibilityRecord.Status.COMPENSATED);
        dsl.thenChainIs("S1", "SE101", ChainStatus.FAILED_AT_B);
    }

    @Test
    void cFailsAfterAAndB_compensatesBThenA() {
        dsl.givenStudentWantsCourse("B1", "SE101");
        dsl.whenStudentEnrollsExpectingFailure("B1", "SE101");
        dsl.thenEligibilityIs("B1", EligibilityRecord.Status.COMPENSATED);
        dsl.thenSeatIs("B1", "SE101", SeatReservation.Status.COMPENSATED);
        dsl.thenChainIs("B1", "SE101", ChainStatus.FAILED_AT_C);
    }

    @Test
    void aFailsBeforeAnythingCommits() {
        dsl.givenStudentWantsCourse("H1", "SE101");
        dsl.whenStudentEnrollsExpectingFailure("H1", "SE101");
        dsl.thenEligibilityIs("H1", EligibilityRecord.Status.REJECTED);
        dsl.thenChainIs("H1", "SE101", ChainStatus.FAILED_AT_A);
    }
}