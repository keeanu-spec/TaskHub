package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateTaskStatusRequest {

    @NotBlank(message = "El status no puede estar vacio")
    private String status; // TODO, IN_PROGRESS, DONE, CANCELLED

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
