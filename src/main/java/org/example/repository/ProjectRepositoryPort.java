package org.example.repository;

import org.example.domain.Project;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto para persistencia de Proyecto.
 */
public interface ProjectRepositoryPort {
    Project save(Project project);
    Optional<Project> findById(UUID id);
    List<Project> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
    List<Project> findByOwnerId(UUID ownerId);
}
