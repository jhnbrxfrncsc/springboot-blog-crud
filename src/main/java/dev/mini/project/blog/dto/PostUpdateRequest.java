package dev.mini.project.blog.dto;

import lombok.Data;

@Data
public class PostUpdateRequest {

    private String title;
    private String content;
    private boolean published;

}
