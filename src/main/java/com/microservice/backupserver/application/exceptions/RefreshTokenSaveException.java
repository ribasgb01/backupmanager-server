package com.microservice.backupserver.application.exceptions;

public class RefreshTokenSaveException extends RuntimeException {
    public RefreshTokenSaveException(String message) {
        super(message);
    }
}
