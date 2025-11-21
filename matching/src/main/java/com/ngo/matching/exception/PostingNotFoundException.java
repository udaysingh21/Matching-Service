package com.ngo.matching.exception;

public class PostingNotFoundException extends RuntimeException {
    public PostingNotFoundException(String message) {
        super(message);
    }
}