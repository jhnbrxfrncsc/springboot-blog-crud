package dev.mini.project.blog.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Optional;

@Data
public class UserUpdateRequest {

    @Size(min = 3, max = 20, message="Username must be between 3 and 20 characters.")
    private String username;

    @Email(message = "Email is invalid.")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters.")
    private String password;
}
