package dev.mini.project.blog.model.dto;

import lombok.Data;

@Data
public class PostUpdateRequest {

    private String title;
    private String content;
    private boolean published;

}
