package org.example.service;

import org.example.domain.Project;
import org.example.domain.User;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ValidationException;
import org.example.repository.ProjectRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepositoryPort projectRepository;

    public ProjectService(ProjectRepositoryPort projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project create(String name, String description, User owner) {
        if (name.isBlank()) {
            throw new ValidationException("El nombre del proyecto no puede estar vacío");
        }
        Project project = new Project(name, description, owner);
        project.getMembers().add(owner);
        return projectRepository.save(project);
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public List<Project> findByFolderPath(String folderPath) {
        return projectRepository.findByFolderPath(folderPath);
    }

    /** Crea un proyecto en una carpeta específica del filesystem virtual. */
    public Project createInFolder(String name, String description, User owner, String folderPath) {
        Project p = new Project(name, description, owner);
        p.setFolderPath(folderPath);
        return projectRepository.save(p);
    }

    public List<Project> findByOwnerId(UUID ownerId) {
        return projectRepository.findByOwnerId(ownerId);
    }

    public Project findById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado: " + id));
    }

    public void addMember(UUID projectId, User user) {
        Project project = findById(projectId);
        project.getMembers().add(user);
        projectRepository.save(project);
    }

    public void deleteById(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new EntityNotFoundException("Proyecto no encontrado: " + id);
        }
        projectRepository.deleteById(id);
    }
}
