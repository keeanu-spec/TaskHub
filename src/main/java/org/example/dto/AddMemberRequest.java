package org.example.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AddMemberRequest {

    @NotNull(message = "El userId no puede ser null")
    private UUID userId;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
