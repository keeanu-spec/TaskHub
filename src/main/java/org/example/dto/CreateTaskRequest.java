package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public class CreateTaskRequest {

    @NotBlank(message = "El titulo no puede estar vacio")
    @Size(max = 200, message = "El titulo no puede superar 200 caracteres")
    private String title;

    @Size(max = 1000, message = "La descripcion no puede superar 1000 caracteres")
    private String description;

    private String priority; // LOW, MEDIUM, HIGH, CRITICAL - por defecto MEDIUM

    private LocalDate dueDate; // nullable

    private UUID assigneeId; // nullable

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public UUID getAssigneeId() { return assigneeId; }
    public void setAssigneeId(UUID assigneeId) { this.assigneeId = assigneeId; }
}
