package com.university.registration;

import java.util.Scanner;

import com.university.registration.exception.ChainException;
import com.university.registration.facade.AdminFacade;
import com.university.registration.facade.RegistrationFacade;
import com.university.registration.persistence.Database;
import com.university.registration.subsystemA.EligibilityRepository;
import com.university.registration.subsystemA.EligibilityService;
import com.university.registration.subsystemA.HoldValidator;
import com.university.registration.subsystemA.PrerequisiteChecker;
import com.university.registration.subsystemB.SeatAllocator;
import com.university.registration.subsystemB.SeatRepository;
import com.university.registration.subsystemB.SeatService;
import com.university.registration.subsystemB.WaitlistManager;
import com.university.registration.subsystemC.BillingCalculator;
import com.university.registration.subsystemC.EnrollmentRepository;
import com.university.registration.subsystemC.EnrollmentService;
import com.university.registration.subsystemC.TranscriptValidator;

public class Main {
    public static void main(String[] args) {
        Database db = new Database();
        db.initialize();

        PrerequisiteChecker pc = new PrerequisiteChecker();
        HoldValidator hv = new HoldValidator();
        EligibilityRepository aRepo = new EligibilityRepository(db);
        EligibilityService aService = new EligibilityService(pc, hv, aRepo);

        SeatAllocator allocator = new SeatAllocator(2);
        WaitlistManager wm = new WaitlistManager();
        SeatRepository bRepo = new SeatRepository(db);
        SeatService bService = new SeatService(allocator, wm, bRepo);

        BillingCalculator bc = new BillingCalculator();
        TranscriptValidator tv = new TranscriptValidator();
        EnrollmentRepository cRepo = new EnrollmentRepository(db);
        EnrollmentService cService = new EnrollmentService(bc, tv, cRepo);

        RegistrationFacade registration = new RegistrationFacade(aService, bService, cService, db);
        AdminFacade admin = new AdminFacade(aService, bService, cService, aRepo, bRepo, cRepo, registration);

        System.out.println("Startup: use menu to enroll or inspect.");

        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("""

                University Registration
                1) Enroll student in course
                2) Inspect persisted state
                3) Manual compensation
                4) Resume a chain (after restart)
                5) Quit
                """);
            System.out.print("> ");
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        System.out.print("Student ID: "); String s = in.nextLine().trim();
                        System.out.print("Course ID:  "); String c = in.nextLine().trim();
                        registration.enroll(s, c);
                        System.out.println("Chain completed.");
                    }
                    case "2" -> {
                        System.out.print("Student ID: "); String s = in.nextLine().trim();
                        System.out.print("Course ID:  "); String c = in.nextLine().trim();
                        System.out.println(admin.inspect(s, c));
                    }
                    case "3" -> {
                        System.out.print("Student ID: "); String s = in.nextLine().trim();
                        System.out.print("Course ID:  "); String c = in.nextLine().trim();
                        admin.forceCompensate(s, c);
                        System.out.println("Compensation requested.");
                    }
                    case "4" -> {
                        System.out.print("Student ID: "); String s = in.nextLine().trim();
                        System.out.print("Course ID:  "); String c = in.nextLine().trim();
                        registration.resume(s, c);
                        System.out.println("Resume done. State now:");
                        System.out.println(admin.inspect(s, c));
                    }
                    case "5" -> { return; }
                    default -> System.out.println("?");
                }
            } catch (ChainException e) {
                System.out.println("Chain failed: " + e.getMessage());
                System.out.println("   (facade has already compensated the committed portion)");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}