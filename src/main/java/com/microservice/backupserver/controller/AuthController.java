package com.microservice.backupserver.controller;

import com.microservice.backupserver.application.usecases.AuthUseCase;
import com.microservice.backupserver.application.usecases.dto.AuthenticationDTO;
import com.microservice.backupserver.application.usecases.dto.UserDTO;
import com.microservice.backupserver.controller.dto.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest){

        AuthenticationDTO authResponse = authUseCase.authenticate(loginRequest.username(), loginRequest.password());

        Duration accessTtl = Duration.between(Instant.now(), authResponse.accessExpiration());
        Duration refreshTtl = Duration.between(Instant.now(), authResponse.refreshExpiration());

        ResponseCookie accessCookie = ResponseCookie
                .from("accessToken", authResponse.accessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(accessTtl)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", authResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTtl)
                .sameSite("Lax")
                .build();

        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.noContent()
                .headers(headers)
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refreshToken", required = false)
            String refreshToken){

        AuthenticationDTO authResponse = authUseCase.rotateRefreshToken(refreshToken);

        Duration accessTtl = Duration.between(Instant.now(), authResponse.accessExpiration());
        Duration refreshTtl = Duration.between(Instant.now(), authResponse.refreshExpiration());

        ResponseCookie accessCookie = ResponseCookie
                .from("accessToken", authResponse.accessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(accessTtl)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", authResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTtl)
                .sameSite("Lax")
                .build();

        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.noContent()
                .headers(headers)
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false)
            String refreshToken
    ){

        ResponseCookie accessCookie = ResponseCookie
                .from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        try {
            if(refreshToken != null && !refreshToken.isBlank()){
                authUseCase.logout(refreshToken);
            }

        } catch (Exception e){
            return ResponseEntity.internalServerError()
                    .headers(headers)
                    .build();
        }

        return ResponseEntity.noContent()
                .headers(headers)
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            @CookieValue(name = "accessToken", required = false)
            String accessToken
    ){
        UserDTO user = authUseCase.getCurrentUser(accessToken);
        return ResponseEntity.ok(user);

    }
}
