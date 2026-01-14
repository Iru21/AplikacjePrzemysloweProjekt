package me.iru.datingapp.controller.web;

import me.iru.datingapp.config.SecurityConfig;
import me.iru.datingapp.dto.UserRegistrationDto;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthWebController.class)
@Import({SecurityConfig.class})
class AuthWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setEmail("newuser@example.com");
        registrationDto.setPassword("Password123!");
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setGender(User.Gender.MALE);
        registrationDto.setAge(25);
        registrationDto.setCity("Warsaw");
    }

    @Test
    @WithAnonymousUser
    void testShowRegistrationForm_Success() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userRegistrationDto"));
    }

    @Test
    void testShowRegistrationForm_RedirectsWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/register")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    @WithAnonymousUser
    void testRegisterUser_Success() throws Exception {
        when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(null);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "newuser@example.com")
                        .param("password", "Password123!")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("gender", "MALE")
                        .param("age", "25")
                        .param("city", "Warsaw"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("successMessage", "Registration successful! Please login."));

        verify(userService, times(1)).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    @WithAnonymousUser
    void testRegisterUser_ValidationFails_InvalidEmail() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "invalid-email")
                        .param("password", "Password123!")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("gender", "MALE")
                        .param("age", "25")
                        .param("city", "Warsaw"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        verify(userService, never()).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    @WithAnonymousUser
    void testRegisterUser_ValidationFails_AgeTooYoung() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "newuser@example.com")
                        .param("password", "Password123!")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("gender", "MALE")
                        .param("age", "15")
                        .param("city", "Warsaw"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        verify(userService, never()).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    @WithAnonymousUser
    void testRegisterUser_ServiceThrowsException() throws Exception {
        when(userService.registerUser(any(UserRegistrationDto.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "newuser@example.com")
                        .param("password", "Password123!")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("gender", "MALE")
                        .param("age", "25")
                        .param("city", "Warsaw"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("errorMessage", "Email already exists"));

        verify(userService, times(1)).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    @WithAnonymousUser
    void testShowLoginForm_Success() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testShowLoginForm_RedirectsWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/login")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    @WithAnonymousUser
    void testRegisterUser_ValidationFails_EmptyFields() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "")
                        .param("password", "")
                        .param("firstName", "")
                        .param("lastName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        verify(userService, never()).registerUser(any(UserRegistrationDto.class));
    }
}

