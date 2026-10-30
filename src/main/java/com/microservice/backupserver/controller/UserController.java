package com.microservice.backupserver.controller;

import com.microservice.backupserver.application.usecases.UserUseCase;
import com.microservice.backupserver.controller.dto.request.CreateUserRequest;
import com.microservice.backupserver.controller.dto.request.DeleteUserRequest;
import com.microservice.backupserver.controller.dto.response.UserResponse;
import com.microservice.backupserver.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    @PostMapping("/create")
    public ResponseEntity<Void> createUser(@RequestBody @Valid CreateUserRequest request){

        userUseCase.createUser(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestBody @Valid DeleteUserRequest request){

        userUseCase.deleteUser(request.userId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getActiveUsers(){

        List<UserResponse> userList = userUseCase.getActiveUsers()
                .stream().map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getRole(),
                        user.getActive(),
                        user.getCreatedAt()
                )).toList();

        return ResponseEntity.ok(userList);
    }
}
