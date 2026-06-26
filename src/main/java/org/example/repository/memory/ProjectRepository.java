package org.example.repository.memory;

import org.example.domain.Project;
import org.example.repository.InMemoryRepository;
import java.util.UUID;

public class ProjectRepository extends InMemoryRepository<Project,UUID>{

    public ProjectRepository() {
        super(project -> project.getId());
    }
    
}
