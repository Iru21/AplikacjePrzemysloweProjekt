package me.iru.datingapp.service;

import me.iru.datingapp.dto.LoginDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.InvalidCredentialsException;
import me.iru.datingapp.mapper.UserMapper;
import me.iru.datingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private LoginDto loginDto;
    private UserProfileDto profileDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.Role.USER);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        profileDto = new UserProfileDto();
        profileDto.setId(1L);
        profileDto.setEmail("test@example.com");
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userMapper.toDto(any(User.class))).thenReturn(profileDto);

        UserProfileDto result = authenticationService.login(loginDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(userMapper).toDto(testUser);
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(loginDto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userMapper, never()).toDto(any(User.class));
    }

    @Test
    void testLogin_InvalidPassword() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(loginDto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(userMapper, never()).toDto(any(User.class));
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserDetails result = authenticationService.loadUserByUsername("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.loadUserByUsername("notfound@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("notfound@example.com");
    }

    @Test
    void testLoadUserByUsername_WithAdminRole() {
        testUser.setRole(User.Role.ADMIN);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserDetails result = authenticationService.loadUserByUsername("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void testLogin_VerifiesPasswordIsNotPlaintext() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(userMapper.toDto(any(User.class))).thenReturn(profileDto);

        authenticationService.login(loginDto);

        verify(passwordEncoder).matches("password123", "encodedPassword");
        assertThat(testUser.getPassword()).isNotEqualTo("password123");
    }

    @Test
    void testLogin_NullEmail() {
        loginDto.setEmail(null);

        assertThatThrownBy(() -> authenticationService.login(loginDto))
                .isInstanceOf(Exception.class);
    }

    @Test
    void testLogin_EmptyPassword() {
        loginDto.setPassword("");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(loginDto))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}

