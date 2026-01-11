package me.iru.datingapp.service;

import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.dto.UserRegistrationDto;
import me.iru.datingapp.dto.UserUpdateDto;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.FileStorageException;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.exception.UserAlreadyExistsException;
import me.iru.datingapp.mapper.UserMapper;
import me.iru.datingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationDto registrationDto;
    private UserProfileDto profileDto;
    private UserUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setGender(User.Gender.MALE);
        testUser.setAge(25);
        testUser.setCity("Warsaw");
        testUser.setRole(User.Role.USER);

        registrationDto = new UserRegistrationDto();
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("password123");
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setGender(User.Gender.MALE);
        registrationDto.setAge(25);
        registrationDto.setCity("Warsaw");

        profileDto = new UserProfileDto();
        profileDto.setId(1L);
        profileDto.setEmail("test@example.com");
        profileDto.setFirstName("John");
        profileDto.setLastName("Doe");

        updateDto = new UserUpdateDto();
        updateDto.setBio("New bio");
        updateDto.setCity("Krakow");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserRegistrationDto.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(any(User.class))).thenReturn(profileDto);

        UserProfileDto result = userService.registerUser(registrationDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(registrationDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(any(User.class))).thenReturn(profileDto);

        UserProfileDto result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
        verify(userMapper).toDto(testUser);
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(999L);
        verify(userMapper, never()).toDto(any(User.class));
    }

    @Test
    void testGetAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDto(any(User.class))).thenReturn(profileDto);

        Page<UserProfileDto> result = userService.getAllUsers(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findAll(pageable);
    }

    @Test
    void testUpdateUserProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(any(User.class))).thenReturn(profileDto);
        doNothing().when(userMapper).updateEntityFromDto(any(UserUpdateDto.class), any(User.class));

        UserProfileDto result = userService.updateUserProfile(1L, updateDto);

        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(userMapper).updateEntityFromDto(updateDto, testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateUserProfile_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserProfile(999L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser_CascadeDelete() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        userService.deleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void testUploadProfilePhoto_Success() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        String filename = "photo.jpg";
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(fileStorageService.store(any(MultipartFile.class))).thenReturn(filename);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        String result = userService.uploadProfilePhoto(1L, file);

        // Then
        assertThat(result).isEqualTo("/uploads/" + filename);
        verify(fileStorageService).store(file);
        verify(userRepository).save(testUser);
    }

    @Test
    void testUploadProfilePhoto_UserNotFound() {
        MultipartFile file = mock(MultipartFile.class);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.uploadProfilePhoto(999L, file))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository).findById(999L);
        verify(fileStorageService, never()).store(any(MultipartFile.class));
    }

    @Test
    void testUploadProfilePhoto_FileStorageException() {
        MultipartFile file = mock(MultipartFile.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(fileStorageService.store(any(MultipartFile.class)))
                .thenThrow(new FileStorageException("Storage error"));

        assertThatThrownBy(() -> userService.uploadProfilePhoto(1L, file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Storage error");

        verify(fileStorageService).store(file);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(any(User.class))).thenReturn(profileDto);

        UserProfileDto result = userService.getUserByEmail("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("notfound@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void testPasswordIsEncoded() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserRegistrationDto.class))).thenReturn(testUser);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(any(User.class))).thenReturn(profileDto);

        userService.registerUser(registrationDto);

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("$2a$10$encodedPassword")
        ));
    }
}

