package dev.mini.project.blog.service;

import dev.mini.project.blog.dto.*;
import dev.mini.project.blog.entity.Post;
import dev.mini.project.blog.entity.User;
import dev.mini.project.blog.mapper.PostMapper;
import dev.mini.project.blog.repository.PostRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

/**
 * Service class for Post entity.
 *
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final UserService userService;
    private final PostMapper postMapper;

    /**
     * return all post record.
     *
     * @return {@link List}
     * @see List
     * @see PostData
     */
    @Transactional(readOnly = true)
    public PageResponse<PostData> getAllPosts(int page, int size, String sortDirection) throws IllegalArgumentException {
        Sort sort = parseSortDirection(sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PostData> postData = postRepository.findAll(pageable)
                .map(postMapper::convertToPostDTO);

        return new PageResponse<>(postData);
    }

    /**
     * search posts
     *
     * @param query         query
     * @param page          page
     * @param size          size
     * @param sortDirection sortDirection
     * @return {@link PageResponse}
     * @throws IllegalArgumentException java.lang. illegal argument exception
     * @see PageResponse
     * @see PostData
     */
    @Transactional(readOnly = true)
    public PageResponse<PostData> searchPosts(String query, int page, int size, String sortDirection) throws IllegalArgumentException {
        Sort sort = parseSortDirection(sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PostData> posts = postRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query, pageable)
                .map(postMapper::convertToPostDTO);

        return new PageResponse<>(posts);
    }

    /**
     * get post by id
     *
     * @param userId userId
     * @return {@link PostData}
     * @see PostData
     */
    @Transactional(readOnly = true)
    public PostData getPostById(Integer userId) throws ValidationException {
        Post post = getSinglePost(userId);
        return postMapper.convertToPostDTO(post);
    }

    /**
     * create post
     *
     * @param postCreateRequest postCreateRequest
     * @return {@link PostData}
     * @throws ValidationException jakarta.validation. validation exception
     * @see PostData
     */
    @Transactional
    public PostData createPost(PostCreateRequest postCreateRequest) throws ValidationException {
        logger.info("PostService#createPost() -- START");
        // Check if passed author ID is existing.
        User author = userService.getUserEntityById(postCreateRequest.getAuthorId());
        Post newPost = postMapper.convertToPostEntity(postCreateRequest, author);

        // persist
        Post savedPost = postRepository.save(newPost);
        logger.info("PostService#createPost() -- successfully saved the post: {}", savedPost.getTitle());

        logger.info("PostService#createPost() -- END");
        return postMapper.convertToPostDTO(savedPost);
    }

    /**
     * update post
     *
     * @param postId        postId
     * @param updateRequest updateRequest
     * @return {@link PostData}
     * @throws ValidationException jakarta.validation. validation exception
     * @see PostData
     */
    @Transactional
    public PostData updatePost(Integer postId, PostUpdateRequest updateRequest) throws ValidationException {
        logger.info("PostService#updatePost() -- START");
        // fetch entity record
        Post existingPost = getSinglePost(postId);
        boolean updated = false;

        // perform validation before updating the existing post record fields.
        updated |= updateIfChanged(existingPost.getTitle(), updateRequest.getTitle(), existingPost::setTitle);
        updated |= updateIfChanged(existingPost.getContent(), updateRequest.getContent(), existingPost::setContent);

        if (existingPost.isPublished() != updateRequest.isPublished()) {
            existingPost.setPublished(updateRequest.isPublished());
            updated = true;
        }

        // if values weren't updated, return the unchanged post record.
        if (!updated) {
            logger.info("PostService#updatePost() -- No changes for post {} -- END", postId);
            return postMapper.convertToPostDTO(existingPost);
        }

        // convert to DTO
        logger.info("PostService#updatePost() -- END");
        return postMapper.convertToPostDTO(existingPost);
    }

    /**
     * delete post
     *
     * @param userId userId
     * @throws ValidationException jakarta.validation. validation exception
     */
    @Transactional
    public void deletePost(Integer userId) throws ValidationException {
        logger.info("PostService#deletePost() -- START");
        Post post = getSinglePost(userId);

        postRepository.delete(post);
        logger.info("PostService#deletePost() -- successfully deleted the post: {}", post.getTitle());
        logger.info("PostService#deletePost() -- END");
    }

    /**
     * get single post
     *
     * @param id id
     * @return {@link Post}
     * @see Post
     */
    public Post getSinglePost(Integer id) {
        return postRepository.findById(id)
                .orElseThrow(() -> {
                    String message = "Post with id " + id + " not found";
                    logger.error("PostService#getSinglePost -- {}",message);
                    return new ValidationException(message);
                });
    }

    /**
     * parse sort direction
     *
     * @param sortDirection sortDirection
     * @return {@link Sort}
     * @see Sort
     */
    private Sort parseSortDirection(String sortDirection) {
        return switch (sortDirection.toLowerCase()) {
            case "asc", "ascending" -> Sort.by("id").ascending();
            case "desc", "descending" -> Sort.by("id").descending();
            default -> {
                    logger.error("Invalid sortDirection {}", sortDirection);
                    throw new IllegalArgumentException("Invalid sortDirection: " + sortDirection);
            }
        };
    }

    /**
     * update if changed
     *
     * @param currentValue currentValue
     * @param newValue newValue
     * @param setter setter
     * @return {@link boolean}
     */
    private boolean updateIfChanged(String currentValue, String newValue, Consumer<String> setter) {
        if (newValue != null && !newValue.equals(currentValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

}
