package me.iru.datingapp.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.iru.datingapp.dto.LoginDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.dto.UserRegistrationDto;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.InvalidCredentialsException;
import me.iru.datingapp.exception.UserAlreadyExistsException;
import me.iru.datingapp.service.AuthenticationService;
import me.iru.datingapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@Import(ObjectMapper.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationService authenticationService;

    private UserRegistrationDto registrationDto;
    private LoginDto loginDto;
    private UserProfileDto userProfileDto;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setEmail("newuser@example.com");
        registrationDto.setPassword("Password123!");
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setGender(User.Gender.MALE);
        registrationDto.setAge(25);
        registrationDto.setCity("Warsaw");

        loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("Password123!");

        userProfileDto = new UserProfileDto();
        userProfileDto.setId(1L);
        userProfileDto.setEmail("test@example.com");
        userProfileDto.setFirstName("John");
        userProfileDto.setLastName("Doe");
        userProfileDto.setGender(User.Gender.MALE);
        userProfileDto.setAge(25);
        userProfileDto.setCity("Warsaw");
        userProfileDto.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithAnonymousUser
    void testRegister_Success() throws Exception {
        when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(userProfileDto);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));

        verify(userService, times(1)).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    @WithAnonymousUser
    void testRegister_EmailAlreadyExists() throws Exception {
        when(userService.registerUser(any(UserRegistrationDto.class)))
                .thenThrow(new UserAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    @WithAnonymousUser
    void testRegister_ValidationFails_MissingEmail() throws Exception {
        registrationDto.setEmail(null);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    @WithAnonymousUser
    void testRegister_ValidationFails_InvalidEmail() throws Exception {
        registrationDto.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    @WithAnonymousUser
    void testRegister_ValidationFails_PasswordTooShort() throws Exception {
        registrationDto.setPassword("pass");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    @WithAnonymousUser
    void testRegister_ValidationFails_AgeTooYoung() throws Exception {
        registrationDto.setAge(17);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    @WithAnonymousUser
    void testLogin_Success() throws Exception {
        when(authenticationService.login(any(LoginDto.class))).thenReturn(userProfileDto);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.firstName", is("John")));

        verify(authenticationService, times(1)).login(any(LoginDto.class));
    }

    @Test
    @WithAnonymousUser
    void testLogin_InvalidCredentials() throws Exception {
        when(authenticationService.login(any(LoginDto.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());

        verify(authenticationService, times(1)).login(any(LoginDto.class));
    }

    @Test
    @WithAnonymousUser
    void testLogin_ValidationFails_MissingEmail() throws Exception {
        loginDto.setEmail(null);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any());
    }

    @Test
    @WithAnonymousUser
    void testLogin_ValidationFails_MissingPassword() throws Exception {
        loginDto.setPassword(null);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any());
    }
}

