package com.microservice.backupserver.application.exceptions;

public class StorageOperationException extends RuntimeException{
    public StorageOperationException(String message, Throwable cause){
        super(message, cause);
    }
}
