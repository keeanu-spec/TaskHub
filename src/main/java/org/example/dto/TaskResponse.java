package org.example.dto;

import org.example.domain.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        String status,
        String priority,
        String assigneeUsername,
        String projectName,
        LocalDate dueDate,
        LocalDateTime createdAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getPriority().name(),
                task.getAssignee() != null ? task.getAssignee().getUsername() : null,
                task.getProject().getName(),
                task.getDueDate(),
                task.getCreatedAt()
        );
    }
}
