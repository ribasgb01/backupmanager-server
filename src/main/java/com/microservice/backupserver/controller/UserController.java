package com.microservice.backupserver.controller;

import com.microservice.backupserver.application.usecases.UserUseCase;
import com.microservice.backupserver.controller.dto.request.CreateUserRequest;
import com.microservice.backupserver.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody @Valid CreateUserRequest request){

        userUseCase.createUser(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
