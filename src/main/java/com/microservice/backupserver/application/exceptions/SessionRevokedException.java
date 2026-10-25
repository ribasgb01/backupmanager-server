package com.microservice.backupserver.application.exceptions;

public class SessionRevokedException extends RuntimeException {
    public SessionRevokedException(String message) {
        super(message);
    }
}
