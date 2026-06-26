package org.example.repository.memory;

import org.example.domain.Project;
import org.example.repository.InMemoryRepository;
import org.example.repository.ProjectRepositoryPort;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProjectRepository extends InMemoryRepository<Project, UUID> implements ProjectRepositoryPort {

    public ProjectRepository() {
        super(project -> project.getId());
    }

    @Override
    public List<Project> findByOwnerId(UUID ownerId) {
        return findAll().stream()
            .filter(p -> p.getOwner() != null && p.getOwner().getId().equals(ownerId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Project> findByFolderPath(String folderPath) {
        return findAll().stream()
            .filter(p -> folderPath.equals(p.getFolderPath()))
            .collect(Collectors.toList());
    }
}
