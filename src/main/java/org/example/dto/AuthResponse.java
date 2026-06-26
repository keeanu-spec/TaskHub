package org.example.dto;

public record AuthResponse(
        String token,
        String type,
        String username,
        String role
) {
    public static AuthResponse of(String token, String username, String role) {
        return new AuthResponse(token, "Bearer", username, role);
    }
}
