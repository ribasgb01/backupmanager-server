package com.microservice.backupserver.application.exceptions;

public class RefreshTokenGenerationException extends RuntimeException {
    public RefreshTokenGenerationException(String message) {
        super(message);
    }
}
