package com.ngo.matching.exception;

public class ServiceCommunicationException extends RuntimeException {
    public ServiceCommunicationException(String message) {
        super(message);
    }
}