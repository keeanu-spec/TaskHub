package org.example.repository.json;

import java.nio.file.Path;
import java.util.UUID;

import org.example.domain.Project;

public class ProjectJsonRepository extends JsonRepository<Project,UUID>{
    
    public ProjectJsonRepository(){ 
        super(Project::getId,Path.of("data/prjects.json"),Project.class);
    }
    
}
