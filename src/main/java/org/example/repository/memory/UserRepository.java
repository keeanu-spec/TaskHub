package org.example.repository.memory;

import org.example.domain.User;
import org.example.repository.InMemoryRepository;
import org.example.repository.UserRepositoryPort;

import java.util.Optional;
import java.util.UUID;

public class UserRepository extends InMemoryRepository<User, UUID> implements UserRepositoryPort {

    public UserRepository() {
        super(User::getId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }
}
