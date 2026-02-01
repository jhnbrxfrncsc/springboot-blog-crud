package dev.mini.project.blog.controller;

import dev.mini.project.blog.dto.*;
import dev.mini.project.blog.service.PostService;
import jakarta.persistence.PostUpdate;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SortDirection;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    public final PostService postService;

    /**
     * get all posts
     *
     * @param page page
     * @param size size
     * @param sort sort
     * @return {@link ResponseData}
     * @see ResponseData
     * @see PageResponse
     */
    @GetMapping("")
    public ResponseData<PageResponse<PostData>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        try {
            PageResponse<PostData> posts = postService.getAllPosts(page, size, sort);
            return new ResponseData<>("Successfully fetched the post records.", HttpStatus.OK, posts);
        } catch (IllegalArgumentException e) {
            return new ResponseData<>(e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }

    }

    /**
     * search posts
     *
     * @param query query
     * @param page page
     * @param size size
     * @param sort sort
     * @return {@link ResponseData}
     * @see ResponseData
     * @see Page
     */
    @GetMapping("/search")
    public ResponseData<PageResponse<PostData>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        try {
            PageResponse<PostData> queryResult = postService.searchPosts(query, page, size, sort);
            return new ResponseData<>("Successfully fetched the post records.", HttpStatus.OK, queryResult);
        } catch(IllegalArgumentException e) {
            return new ResponseData<>(e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    /**
     * get post by id
     *
     * @param id id
     * @return {@link ResponseData}
     * @see ResponseData
     * @see PostData
     */
    @GetMapping("/{id}")
    public ResponseData<PostData> getPostById(@PathVariable Integer id) {
        PostData post = postService.getPostById(id);
        if(post == null) {
            return new ResponseData<>("Server error", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

        return new ResponseData<>("Successfully fetched the post record.", HttpStatus.OK, post);
    }

    /**
     * create post
     *
     * @param postCreateRequest postCreateRequest
     * @param bindingResult bindingResult
     * @return {@link ResponseData}
     * @see ResponseData
     * @see PostData
     */
    @PostMapping("")
    public ResponseData<PostData> createPost(@Valid @RequestBody PostCreateRequest postCreateRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return new ResponseData<>(errors, HttpStatus.BAD_REQUEST, null);
        }

        try {
            PostData newlyCreatedPost = postService.createPost(postCreateRequest);

            return new ResponseData<>("Successfully created the post.", HttpStatus.OK, newlyCreatedPost);
        } catch(ValidationException ve){
            return new ResponseData<>(ve.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    /**
     * update post
     *
     * @param id id
     * @param postUpdateRequest postUpdateRequest
     * @return {@link ResponseData}
     * @see ResponseData
     * @see PostData
     */
    @PutMapping("/{id}")
    public ResponseData<PostData> updatePost(@PathVariable Integer id, @RequestBody PostUpdateRequest postUpdateRequest){
        try {
            PostData updatedPost = postService.updatePost(id, postUpdateRequest);
            return new ResponseData<>("Successfully updated the post.", HttpStatus.OK, updatedPost);
        } catch(ValidationException ve){
            return new ResponseData<>(ve.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    /**
     * delete post
     *
     * @param id id
     * @return {@link ResponseData}
     * @see ResponseData
     * @see PostData
     */
    @DeleteMapping("/{id}")
    public ResponseData<PostData> deletePost(@PathVariable Integer id){
        try {
            postService.deletePost(id);
            return new ResponseData<>("Successfully deleted the post.", HttpStatus.OK, null);
        } catch(ValidationException ve){
            return new ResponseData<>(ve.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }
}
