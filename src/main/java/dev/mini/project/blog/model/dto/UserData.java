package dev.mini.project.blog.model.dto;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserData {
    private Integer id;
    private String username;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PostData> posts = new ArrayList<>();

    public UserData() {}

    public UserData(Integer id, String username, String email, Instant createdAt, Instant updatedAt, List<PostData> posts) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.posts = posts;
    }
}
