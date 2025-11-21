package com.ngo.matching.exception;

public class NoSlotsAvailableException extends RuntimeException {
    public NoSlotsAvailableException(String message) {
        super(message);
    }
}