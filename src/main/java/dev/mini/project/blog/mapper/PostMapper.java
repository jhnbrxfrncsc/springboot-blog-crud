package dev.mini.project.blog.mapper;

import dev.mini.project.blog.dto.PostCreateRequest;
import dev.mini.project.blog.dto.PostData;
import dev.mini.project.blog.entity.Post;
import dev.mini.project.blog.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostMapper {

    /**
     * convert to post entity
     *
     * @param postCreateRequest postCreateRequest
     * @param author author
     * @return {@link Post}
     * @see Post
     */
    public Post convertToPostEntity(PostCreateRequest postCreateRequest, User author){
        return Post.builder()
                .title(postCreateRequest.getTitle())
                .content(postCreateRequest.getContent())
                .author(author)
                .published(postCreateRequest.isPublished())
                .build();
    }

    /**
     * convert entity to dto.
     *
     * @param post post
     * @return {@link PostData}
     * @see PostData
     */
    public PostData convertToPostDTO(Post post) {
        return new PostData(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getId(),
                post.getAuthor().getUsername(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }

    /**
     * convert list of posts to list of post dto
     *
     * @param posts posts
     * @return {@link List}
     * @see List
     * @see PostData
     */
    public List<PostData> toListDTO(List<Post> posts) {
        return posts.isEmpty() ? new ArrayList<>() : posts
                .stream()
                .map(this::convertToPostDTO)
                .collect(Collectors.toList());
    }

}
