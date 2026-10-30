package com.helpdesk.web.dto;

import com.helpdesk.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(
            String token,
            String type,
            UserResponse user
    ) {
    }

    public record UserResponse(
            Long id,
            String name,
            String email,
            Role role,
            boolean active
    ) {
    }

    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 120) String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 72) String password,
            Role role
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank @Size(min = 3, max = 120) String name,
            Role role,
            Boolean active
    ) {
    }
}
