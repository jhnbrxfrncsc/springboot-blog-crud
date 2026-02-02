package dev.mini.project.blog.mapper;

import dev.mini.project.blog.model.dto.UserRegisterRequest;
import dev.mini.project.blog.model.dto.UserData;
import dev.mini.project.blog.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PostMapper postMapper;
    private final PasswordEncoder passwordEncoder;

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
     * @param userRegisterRequest userData
     * @return {@link User}
     * @see User
     */
    public User convertToUserEntity(UserRegisterRequest userRegisterRequest) {
        return User.builder()
                .username(userRegisterRequest.getUsername())
                .email(userRegisterRequest.getEmail())
                .password(passwordEncoder.encode(userRegisterRequest.getPassword()))
                .build();
    }
}
