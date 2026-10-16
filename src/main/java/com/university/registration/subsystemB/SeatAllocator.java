package com.university.registration.subsystemB;

public class SeatAllocator {
    private final int capacity;
    private int reserved = 0;

    public SeatAllocator(int capacity) { this.capacity = capacity; }

    public boolean hasSpace() { return reserved < capacity; }
    public void reserveOne() { reserved++; }
    public void releaseOne() { if (reserved > 0) reserved--; }
}