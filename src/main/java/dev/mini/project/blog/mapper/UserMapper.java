package dev.mini.project.blog.mapper;

import dev.mini.project.blog.dto.UserCreateRequest;
import dev.mini.project.blog.dto.UserData;
import dev.mini.project.blog.entity.User;
import dev.mini.project.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PostMapper postMapper;

    /**
     * Convert to User DTO
     *
     * @param user user
     * @return {@link UserData}
     * @see UserData
     */
    public UserData convertToDTO(User user) {
        return new UserData(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                postMapper.toListDTO(user.getPosts())
        );
    }

    /**
     * convert to user entity
     *
     * @param userCreateRequest userData
     * @return {@link User}
     * @see User
     */
    public User convertToUserEntity(UserCreateRequest userCreateRequest) {
        return User.builder()
                .username(userCreateRequest.getUsername())
                .email(userCreateRequest.getEmail())
                .password(userCreateRequest.getPassword())
                .build();
    }
}
