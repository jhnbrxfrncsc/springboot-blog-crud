package dev.mini.project.blog.model.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class PostData {
    private Integer id;
    private String title;
    private String content;
    private Integer authorId;
    private String authorUsername;
    private Instant createdAt;
    private Instant updatedAt;

    public PostData() {}

    public PostData(Integer id, String title, String content, Integer authorId, String authorUsername, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
