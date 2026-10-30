package com.microservice.backupserver.infrastructure.adapters;

import com.microservice.backupserver.application.gateways.UserGateway;
import com.microservice.backupserver.domain.User;
import com.microservice.backupserver.infrastructure.persistence.UserRepository;
import com.microservice.backupserver.infrastructure.persistence.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserGateway {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {

        return userMapper.toDomain(
                userRepository.save(
                        userMapper.toEntity(user)
                )
        );
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public int deleteByIdReturningCount(UUID id) {
        return userRepository.deleteByIdReturningCount(id);
    }

    @Override
    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue()
                .stream().map(userMapper::toDomain).toList();
    }


}
