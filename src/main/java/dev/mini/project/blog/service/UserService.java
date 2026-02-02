package dev.mini.project.blog.service;

import dev.mini.project.blog.common.UpdateHelper;
import dev.mini.project.blog.config.security.JwtService;
import dev.mini.project.blog.model.dto.*;
import dev.mini.project.blog.model.entity.User;
import dev.mini.project.blog.mapper.UserMapper;
import dev.mini.project.blog.repository.UserRepository;
import dev.mini.project.blog.common.SortUtil;
import dev.mini.project.blog.validation.UserValidator;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    /**
     * get all users
     *
     * @return {@link List}
     * @see List
     * @see UserData
     */
    @Transactional(readOnly = true)
    public PageResponse<UserData> getAllUsers(int page, int size, String sortDirection) throws IllegalArgumentException {
        Sort sort = SortUtil.parseSortDirection(sortDirection, "username");
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
        Sort sort = SortUtil.parseSortDirection(sortDirection, "username");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserData> queryResult = userRepository
                .findByUsernameContainingIgnoreCase(query, pageable)
                .map(userMapper::convertToDTO);

        return new PageResponse<>(queryResult);
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
    public UserData createUser(UserRegisterRequest userRequestData) throws ValidationException {
        logger.info("UserService#createUser() -- START");

        // Validate if user and email already exists.
        userValidator.validateEmail(userRequestData.getUsername());
        userValidator.validateEmail(userRequestData.getEmail());

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

        // validate if username is available.
        userValidator.validateUsername(userRequestData.getUsername());
        updated |= UpdateHelper.updateIfChanged(userRequestData.getUsername(), existingUser.getUsername(), existingUser::setUsername);

        // validate if email is available.
        userValidator.validateEmail(userRequestData.getEmail());
        updated |= UpdateHelper.updateIfChanged(userRequestData.getEmail(), existingUser.getEmail(), existingUser::setEmail);

        // Password update
        if (userRequestData.getPassword() != null && !userRequestData.getPassword().isBlank()) {
            String newPassword = userRequestData.getPassword();
            String oldPassword = existingUser.getPassword();

            if (!passwordEncoder.matches(newPassword, oldPassword)) {
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
     * register user
     *
     * @param userRequestData userRequestData
     * @return {@link AuthResponse}
     * @see AuthResponse
     * @throws ValidationException jakarta.validation. validation exception
     */
    @Transactional
    public AuthResponse registerUser(UserRegisterRequest userRequestData) throws ValidationException {
        logger.info("UserService#registerUser() -- START");
        // Validate if user and email already exists.
        userValidator.validateUsername(userRequestData.getUsername());
        userValidator.validateEmail(userRequestData.getEmail());

        // convert to User entity.
        User convertedUser = userMapper.convertToUserEntity(userRequestData);

        // persist in db
        User savedUser = userRepository.save(convertedUser);
        logger.info("UserService#registerUser() -- Successfully registered user : {}", savedUser.getUsername());

        // generate token
        String token = jwtService.generateToken(savedUser.getEmail());
        logger.info("UserService#registerUser() -- generated token: {}", token);

        logger.info("UserService#registerUser() -- END");
        return new AuthResponse(token);
    }

    /**
     * login user
     *
     * @param userRequestData userRequestData
     * @return {@link AuthResponse}
     * @see AuthResponse
     */
    @Transactional(readOnly = true)
    public AuthResponse loginUser (UserLoginRequest userRequestData) throws UsernameNotFoundException {
        logger.info("UserService#loginUser() -- START");
        User user = getUserEntityByEmail(userRequestData.getEmail());

        logger.info("UserService#loginUser() -- fetched user : {} {}", user.getUsername(), user.getEmail());

        // validate password
        if (!passwordEncoder.matches(userRequestData.getPassword(), user.getPassword())) {
            logger.warn("UserService#loginUser() -- invalid password for email={}", user.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }

        // generate token
        String token = jwtService.generateToken(user.getEmail());
        logger.info("UserService#loginUser() -- generated token: {}", token);

        logger.info("UserService#loginUser() -- END");
        return new AuthResponse(token);
    }

    /**
     * get user entity by id
     *
     * @param email email
     * @return {@link User}
     * @see User
     */
    @Transactional(readOnly = true)
    public User getUserEntityByEmail(String email) {
        logger.info("UserService#getUserEntityByEmail({})", email);
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * get user entity by id
     *
     * @param userId userId
     * @return {@link User}
     * @see User
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(Integer userId) {
        logger.info("UserService#getUserEntityById({})", userId);
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ValidationException("User not found"));
    }

}
