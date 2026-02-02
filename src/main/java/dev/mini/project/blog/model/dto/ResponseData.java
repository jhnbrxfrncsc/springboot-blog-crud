package dev.mini.project.blog.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ResponseData<T> {
    private String message;
    private HttpStatus status;
    private T data;
}
