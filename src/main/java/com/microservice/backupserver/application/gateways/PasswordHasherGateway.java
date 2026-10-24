package com.microservice.backupserver.application.gateways;

public interface PasswordHasherGateway {
    String encode(String rawPassword);
    boolean matches (String rawPassword, String encodedPassword);
}
