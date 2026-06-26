package org.example.dto;

import org.example.domain.Project;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        String ownerUsername,
        int memberCount,
        LocalDateTime createdAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getUsername(),
                project.getMembers().size(),
                project.getCreatedAt()
        );
    }
}
