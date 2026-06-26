package org.example.service;

import org.example.domain.Role;
import org.example.domain.User;
import org.example.exception.DuplicateEntityException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ValidationException;
import org.example.repository.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepositoryPort userRepository;

    public UserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public User create(String username, String email, String passwordHash, Role role) {
        if (username.isBlank() || email.isBlank()) {
            throw new ValidationException("Username y email no pueden estar vacios");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEntityException("Email ya registrado: " + email);
        }
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateEntityException("Username ya registrado: " + username);
        }
        User user = new User(username, email, passwordHash, role);
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));
    }

    public void deleteById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuario no encontrado: " + id);
        }
        userRepository.deleteById(id);
    }
}
