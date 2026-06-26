package org.example.repository;

import org.example.domain.Task;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto para persistencia de Tarea.
 */
public interface TaskRepositoryPort {
    Task save(Task task);
    Optional<Task> findById(UUID id);
    List<Task> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
    List<Task> findByProjectId(UUID projectId);
}
