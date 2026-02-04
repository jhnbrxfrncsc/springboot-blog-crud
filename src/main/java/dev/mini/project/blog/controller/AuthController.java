package dev.mini.project.blog.controller;

import dev.mini.project.blog.config.security.JwtService;
import dev.mini.project.blog.model.dto.AuthResponse;
import dev.mini.project.blog.model.dto.ResponseData;
import dev.mini.project.blog.model.dto.UserLoginRequest;
import dev.mini.project.blog.model.dto.UserRegisterRequest;
import dev.mini.project.blog.repository.UserRepository;
import dev.mini.project.blog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;


    @PostMapping("/register")
    public ResponseData<AuthResponse> register(@Valid @RequestBody UserRegisterRequest request, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return new ResponseData<>(errors, HttpStatus.BAD_REQUEST, null);
        }

        try {
            AuthResponse generatedToken = userService.registerUser(request);
            return new ResponseData<>("User created successfully", HttpStatus.OK, generatedToken);
        } catch (Exception e) {
            return new ResponseData<>(e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    @PostMapping("/login")
    public ResponseData<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        try {
            AuthResponse generatedToke = userService.loginUser(request);
            return new ResponseData<>("User login successfully", HttpStatus.OK, generatedToke);
        } catch (RuntimeException re){
            return new ResponseData<>(re.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }
}
