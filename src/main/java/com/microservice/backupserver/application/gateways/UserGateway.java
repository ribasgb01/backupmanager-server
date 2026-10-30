package com.microservice.backupserver.application.gateways;

import com.microservice.backupserver.domain.User;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserGateway {

    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    int deleteByIdReturningCount(UUID id);
    List<User> getActiveUsers();
}
