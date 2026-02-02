package dev.mini.project.blog.validation;

import dev.mini.project.blog.repository.UserRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("Email is already taken");
        }
    }

    public void validateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new ValidationException("Username is already taken");
        }
    }
}
