package org.example.repository;

import org.example.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID>, ProjectRepositoryPort {
    List<Project> findByOwnerId(UUID ownerId);
    List<Project> findByFolderPath(String folderPath);
}
