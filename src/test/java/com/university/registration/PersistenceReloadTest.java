package com.university.registration;

import com.university.registration.dsl.RegistrationDsl;
import com.university.registration.facade.ChainStatus;
import com.university.registration.subsystemA.EligibilityRecord;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceReloadTest {

    @Test
    void facadeResumesFromPersistedStatusAfterRestart() {
        String dbUrl = "jdbc:sqlite:target/reload-" + UUID.randomUUID() + ".db";

        RegistrationDsl first = new RegistrationDsl(dbUrl, 2);
        first.aRepo.save(new EligibilityRecord("S1", "SE101",
                EligibilityRecord.Status.NEW, null));
        first.aRepo.save(new EligibilityRecord("S1", "SE101",
                EligibilityRecord.Status.CHECKED, "OK"));
        writeChain(first, "S1", "SE101", ChainStatus.PENDING_B);

        RegistrationDsl second = new RegistrationDsl(dbUrl, 2);
        second.registration.resume("S1", "SE101");

        second.thenEligibilityIs("S1", EligibilityRecord.Status.COMPENSATED);
        second.thenChainIs("S1", "SE101", ChainStatus.COMPENSATED);
        assertNull(second.bRepo.find("S1", "SE101"));
    }

    private void writeChain(RegistrationDsl dsl, String s, String c, ChainStatus st) {
        try (var conn = dsl.db.connect();
             var ps = conn.prepareStatement(
                "INSERT INTO chain_progress(student_id, course_id, status, updated_at) VALUES(?,?,?,?)")) {
            ps.setString(1, s); ps.setString(2, c); ps.setString(3, st.name());
            ps.setString(4, java.time.Instant.now().toString());
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}