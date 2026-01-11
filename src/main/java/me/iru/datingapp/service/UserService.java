package me.iru.datingapp.service;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.dto.UserRegistrationDto;
import me.iru.datingapp.dto.UserUpdateDto;
import me.iru.datingapp.entity.Interest;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.entity.UserInterest;
import me.iru.datingapp.exception.FileStorageException;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.exception.UserAlreadyExistsException;
import me.iru.datingapp.mapper.UserMapper;
import me.iru.datingapp.repository.InterestRepository;
import me.iru.datingapp.repository.UserInterestRepository;
import me.iru.datingapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final InterestRepository interestRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;

    /**
     * Registers a new user in the system
     *
     * @param dto User registration data
     * @return UserProfileDto with registered user data
     * @throws UserAlreadyExistsException if email already exists
     */
    public UserProfileDto registerUser(UserRegistrationDto dto) {
        log.info("Attempting to register new user with email: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.error("Registration failed: Email {} already exists", dto.getEmail());
            throw new UserAlreadyExistsException("User with email " + dto.getEmail() + " already exists");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.USER);

        User savedUser = userRepository.save(user);
        log.info("Successfully registered user with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());

        return userMapper.toDto(savedUser);
    }

    /**
     * Retrieves user profile by ID
     *
     * @param id User ID
     * @return UserProfileDto
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserProfileDto getUserById(Long id) {
        log.debug("Fetching user profile for ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });

        return userMapper.toDto(user);
    }

    /**
     * Retrieves all users with pagination
     *
     * @param pageable Pagination parameters
     * @return Page of UserProfileDto
     */
    @Transactional(readOnly = true)
    public Page<UserProfileDto> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination: page {}, size {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<User> users = userRepository.findAll(pageable);
        log.debug("Found {} users", users.getTotalElements());

        return users.map(userMapper::toDto);
    }

    /**
     * Updates user profile
     *
     * @param id  User ID
     * @param dto Updated user data
     * @return Updated UserProfileDto
     * @throws ResourceNotFoundException if user not found
     */
    public UserProfileDto updateUserProfile(Long id, UserUpdateDto dto) {
        log.info("Updating profile for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot update: User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });

        userMapper.updateEntityFromDto(dto, user);

        if (dto.getInterestIds() != null) {
            updateUserInterests(user, dto.getInterestIds());
        }

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated profile for user ID: {}", id);

        return userMapper.toDto(updatedUser);
    }

    /**
     * Deletes a user account with cascading deletion
     *
     * @param id User ID
     * @throws ResourceNotFoundException if user not found
     */
    public void deleteUser(Long id) {
        log.info("Attempting to delete user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot delete: User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            try {
                fileStorageService.delete(user.getPhotoUrl());
            } catch (Exception e) {
                log.warn("Failed to delete profile photo for user {}: {}", id, e.getMessage());
            }
        }

        userRepository.delete(user);
        log.info("Successfully deleted user with ID: {}", id);
    }

    /**
     * Uploads a profile photo for a user
     *
     * @param id   User ID
     * @param file Photo file
     * @return URL/path to an uploaded photo
     * @throws ResourceNotFoundException if user not found
     * @throws FileStorageException      if upload fails
     */
    public String uploadProfilePhoto(Long id, MultipartFile file) {
        log.info("Uploading profile photo for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot upload photo: User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });

        try {
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                fileStorageService.delete(user.getPhotoUrl());
            }

            String filename = fileStorageService.store(file);
            user.setPhotoUrl(filename);
            userRepository.save(user);

            log.info("Successfully uploaded profile photo for user ID: {}", id);
            return filename;

        } catch (FileStorageException e) {
            log.error("Failed to upload profile photo for user ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Helper method to update user interests
     */
    private void updateUserInterests(User user, List<Long> interestIds) {
        log.debug("Updating interests for user ID: {}", user.getId());

        userInterestRepository.deleteByUserId(user.getId());

        for (Long interestId : interestIds) {
            Interest interest = interestRepository.findById(interestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Interest not found with ID: " + interestId));

            UserInterest userInterest = new UserInterest();
            userInterest.setUser(user);
            userInterest.setInterest(interest);
            userInterestRepository.save(userInterest);
        }
    }

    /**
     * Get user by email
     *
     * @param email User email
     * @return User entity
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });
    }
}

