package org.example.repository;

import org.example.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto (interfaz de dominio) para persistencia de Usuario.
 * Implementado por:
 *  - UserRepository  (Spring Data JPA — usado por la API REST)
 *  - JpaUserRepository (EntityManager manual — usado por la CLI)
 */
public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID id);
    List<User> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
