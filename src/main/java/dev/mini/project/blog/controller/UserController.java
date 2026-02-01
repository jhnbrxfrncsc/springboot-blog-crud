package dev.mini.project.blog.controller;

import dev.mini.project.blog.dto.*;
import dev.mini.project.blog.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("")
    public ResponseData<PageResponse<UserData>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        try {
            PageResponse<UserData> users = userService.getAllUsers(page, size, sortDirection);
            return new ResponseData<>("Users retrieved successfully", HttpStatus.OK, users);
        } catch(IllegalArgumentException ve) {
            return new ResponseData<>(ve.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR,null);
        }
    }

    @GetMapping("/search")
    public ResponseData<PageResponse<UserData>> searchUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam String query
    ) {
        try{
            PageResponse<UserData> users = userService.searchUsers(page, size, sortDirection, query);
            String message = users.getSize() <= 0 ? "Empty result" : "Users retrieved successfully";
            return new ResponseData<>(message , HttpStatus.OK, users);
        } catch(IllegalArgumentException e) {
            return new ResponseData<>(e.getMessage(), HttpStatus.BAD_REQUEST,null);
        }
    }

    @GetMapping("/{id}")
    public ResponseData<UserData> getUser(@PathVariable Integer id) {
        try {
            UserData user = userService.getUserById(id);
            return  new ResponseData<UserData>("User retrieved successfully", HttpStatus.OK, user);
        } catch (ValidationException ve) {
            return new ResponseData<UserData>(ve.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    @PostMapping("")
    public ResponseData<UserData> createUser(@Valid @RequestBody UserCreateRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return new ResponseData<>(errors, HttpStatus.BAD_REQUEST, null);
        }

        try {
            UserData createdUser = userService.createUser(request);
            return new ResponseData<>("User created successfully", HttpStatus.OK, createdUser);
        } catch (ValidationException e) {
            return new ResponseData<>(e.getMessage(), HttpStatus.BAD_REQUEST, null);
        } catch (Exception e) {
            return new ResponseData<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    @PutMapping("/{id}")
    public ResponseData<UserData> updateUser(@PathVariable Integer id, @Valid @RequestBody UserUpdateRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return new ResponseData<>(errors, HttpStatus.BAD_REQUEST, null);
        }

        try {
            UserData updatedUser = userService.updateUser(id, request);
            return new ResponseData<>("User updated successfully", HttpStatus.OK, updatedUser);
        } catch (ValidationException e) {
            return new ResponseData<>(e.getMessage(), HttpStatus.BAD_REQUEST, null);
        } catch (Exception e) {
            return new ResponseData<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

    }

    @DeleteMapping("/{id}")
    public ResponseData<UserData> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return new ResponseData<>("User("+ id +") deleted successfully", HttpStatus.OK, null);
        } catch(ValidationException ve){
            return new ResponseData<>(ve.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }
}
