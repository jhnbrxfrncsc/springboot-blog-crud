package dev.mini.project.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostCreateRequest {

    @NotBlank(message="Title is required")
    @Size(min = 3, max = 30, message = "Title must be between 6 and 20 characters")
    private String title;

    @NotBlank(message="Title is required")
    @Size(min = 5, message = "Content must be at least 5 characters")
    private String content;

    @NotNull(message="Author ID is required")
    private Integer authorId;

    @NotNull(message="Published status is required")
    private boolean published;
}
