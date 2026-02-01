package dev.mini.project.blog.service;

import dev.mini.project.blog.dto.PageResponse;
import dev.mini.project.blog.dto.UserCreateRequest;
import dev.mini.project.blog.dto.UserData;
import dev.mini.project.blog.dto.UserUpdateRequest;
import dev.mini.project.blog.entity.User;
import dev.mini.project.blog.mapper.UserMapper;
import dev.mini.project.blog.repository.UserRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * get all users
     *
     * @return {@link List}
     * @see List
     * @see UserData
     */
    @Transactional(readOnly = true)
    public PageResponse<UserData> getAllUsers(int page, int size, String sortDirection) throws IllegalArgumentException {
        Sort sort = parseSortDirection(sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserData> userData = userRepository.findAll(pageable)
                .map(userMapper::convertToDTO);

        return new PageResponse<>(userData);
    }
    /**
     * search users
     *
     * @param page page
     * @param size size
     * @param sortDirection sortDirection
     * @param query query
     * @return {@link PageResponse}
     * @see PageResponse
     * @see UserData
     * @throws IllegalArgumentException java.lang. illegal argument exception
     */
    @Transactional(readOnly = true)
    public PageResponse<UserData> searchUsers(int page, int size, String sortDirection, String query) throws IllegalArgumentException{
        Sort sort = parseSortDirection(sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserData> queryResult = userRepository
                .findByUsernameContainingIgnoreCase(query, pageable)
                .map(userMapper::convertToDTO);

        return new PageResponse<>(queryResult);
    }

    /**
     * parse sort direction
     *
     * @param sortDirection sortDirection
     * @return {@link Sort}
     * @see Sort
     */
    private Sort parseSortDirection(String sortDirection) {
        return switch(sortDirection.toLowerCase()){
            case "asc", "ascending" -> Sort.by("username").ascending();
            case "desc", "descending" -> Sort.by("username").descending();
            default -> {
                logger.error("Invalid sortDirection {}", sortDirection);
                throw new IllegalArgumentException("Invalid sortDirection: " + sortDirection);
            }
        };
    }

    /**
     * get user by id
     *
     * @param userId userId
     * @return {@link UserData}
     * @see UserData
     */
    @Transactional(readOnly = true)
    public UserData getUserById(Integer userId) throws ValidationException{
        logger.info("UserService#getUserById({}) -- START", userId);
        User user = getUserEntityById(userId);
        logger.info("UserService#getUserById({}) -- END", userId);
        return userMapper.convertToDTO(user);
    }

    /**
     * persist a user record.
     *
     * @param userRequestData userRequestData
     * @return {@link UserData}
     * @see UserData
     */
    @Transactional
    public UserData createUser(UserCreateRequest userRequestData) throws ValidationException {
        logger.info("UserService#createUser() -- START");
        // Validate if user and email already exists.
        validateUserRequest(userRequestData);

        // convert to User entity.
        User convertedUser = userMapper.convertToUserEntity(userRequestData);

        // persist in db
        User savedUser = userRepository.save(convertedUser);
        logger.info("UserService#createUser() -- Successfully created user : {}", savedUser.getUsername());

        //convert to dto
        logger.info("UserService#createUser() -- END");
        return userMapper.convertToDTO(savedUser);
    }

    /**
     * update a single user record.
     *
     * @param userRequestData userRequestData
     * @return {@link UserData}
     * @see UserData
     */
    @Transactional
    public UserData updateUser(Integer userId, UserUpdateRequest userRequestData) throws ValidationException {
        logger.info("UserService#updateUser() -- START");
        User existingUser = getUserEntityById(userId);
        boolean updated = false;

        updated |= updateIfChange(userRequestData.getUsername(), existingUser.getUsername(), existingUser::setUsername, "username");
        updated |= updateIfChange(userRequestData.getEmail(), existingUser.getEmail(), existingUser::setEmail, "email");

        // Password update
        if (userRequestData.getPassword() != null && !userRequestData.getPassword().isBlank()) {
            String newPassword = userRequestData.getPassword();
            if (!newPassword.equals(existingUser.getPassword())){
                existingUser.setPassword(newPassword);
                updated = true;
            }
        }

        if(!updated) {
            logger.info("UserService#updateUser() -- no changes -- END");
            return userMapper.convertToDTO(existingUser);
        }

        logger.info("UserService#updateUser() -- END");
        return userMapper.convertToDTO(existingUser);
    }

    /**
     * delete user
     *
     * @param userId userId
     */
    @Transactional
    public void deleteUser(Integer userId) throws ValidationException {
        logger.info("UserService#deleteUser() -- START");
        User userToBeDeleted = getUserEntityById(userId);

        logger.info("UserService#updateUser() -- user to be delete: {}", userToBeDeleted.getUsername());

        userRepository.delete(userToBeDeleted);
        logger.info("UserService#deleteUser() -- END");
    }

    /**
     * validate if user or email is already taken.
     *
     * @param userRequestData userRequestData
     */
    public void validateUserRequest(UserCreateRequest userRequestData) {
        if (userRepository.existsByUsername(userRequestData.getUsername())) {
            logger.info("UserService#updateUser() -- Username({}) is already taken...", userRequestData.getUsername());
            throw new ValidationException("Username is already taken");
        }
        if (userRepository.existsByEmail(userRequestData.getEmail())) {
            logger.info("UserService#updateUser() -- Email({}) is already taken...", userRequestData.getEmail());
            throw new ValidationException("Email is already taken");
        }
    }


    /**
     * get user entity by id
     *
     * @param userId userId
     * @return {@link User}
     * @see User
     */
    public User getUserEntityById(Integer userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ValidationException("User not found"));
    }


    /**
     * get all users and convert it to User DTO.
     *
     * @return {@link List}
     * @see List
     * @see UserData
     */
    public List<UserData> getAllUsersDTO() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::convertToDTO)
                .collect(Collectors.toList());
    }

    private boolean updateIfChange(String newValue, String currentValue, Consumer<String> setter, String flag) throws ValidationException {
        if (newValue != null && !newValue.isBlank() && !newValue.equals(currentValue)) {
            validateFieldChanges(newValue, flag);
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    private void validateFieldChanges(String fieldValue, String flag){
        switch(flag.toLowerCase()){
            case "email" -> {
                if (userRepository.existsByEmail(fieldValue)) {
                    logger.info("UserService#updateUser() -- Username({}) is already taken...", fieldValue);
                    throw new ValidationException("Email is already taken");
                }
            }
            case "username" -> {
                if (userRepository.existsByUsername(fieldValue)) {
                    logger.info("UserService#updateUser() -- Username({}) is already taken...", fieldValue);
                    throw new ValidationException("Username is already taken");
                }
            }
            default -> {
                throw new ValidationException("Invalid field value");
            }
        }
    }
}
