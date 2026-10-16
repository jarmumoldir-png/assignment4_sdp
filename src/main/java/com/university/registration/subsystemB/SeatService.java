package com.university.registration.subsystemB;

import com.university.registration.exception.SubsystemBFailure;

public class SeatService {
    private final SeatAllocator allocator;
    private final WaitlistManager waitlist;
    private final SeatRepository repository;

    public SeatService(SeatAllocator a, WaitlistManager w, SeatRepository r) {
        this.allocator = a; this.waitlist = w; this.repository = r;
    }

    public SeatReservation allocate(String studentId, String courseId) {
        SeatReservation r = new SeatReservation(studentId, courseId,
                SeatReservation.Status.NEW, null);
        repository.save(r);

        if (allocator.hasSpace()) {
            allocator.reserveOne();
            r.transitionTo(SeatReservation.Status.RESERVED, "SEAT_GRANTED");
            repository.save(r);
            return r;
        }
        if (waitlist.canWaitlist(studentId)) {
            r.transitionTo(SeatReservation.Status.WAITLISTED, "WAITLISTED");
            repository.save(r);
            throw new SubsystemBFailure("Course full, waitlisted: " + courseId);
        }
        r.transitionTo(SeatReservation.Status.REJECTED, "COURSE_FULL");
        repository.save(r);
        throw new SubsystemBFailure("Course full, cannot waitlist: " + courseId);
    }

    public void compensate(String studentId, String courseId) {
        SeatReservation r = repository.find(studentId, courseId);
        if (r == null) return;
        if (r.getStatus() == SeatReservation.Status.RESERVED) {
            allocator.releaseOne();
            r.transitionTo(SeatReservation.Status.COMPENSATED, "ROLLED_BACK");
            repository.save(r);
        } else if (r.getStatus() == SeatReservation.Status.WAITLISTED) {
            r.transitionTo(SeatReservation.Status.COMPENSATED, "ROLLED_BACK");
            repository.save(r);
        }
    }
}