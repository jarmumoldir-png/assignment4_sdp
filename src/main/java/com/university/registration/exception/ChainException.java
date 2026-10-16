package com.university.registration.exception;

public class ChainException extends RuntimeException {
    public ChainException(String msg) { super(msg); }
    public ChainException(String msg, Throwable cause) { super(msg, cause); }
}