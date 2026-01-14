package me.iru.datingapp.controller.web;

import me.iru.datingapp.config.SecurityConfig;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.dto.UserUpdateDto;
import me.iru.datingapp.entity.Interest;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.service.InterestService;
import me.iru.datingapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileWebController.class)
@Import({SecurityConfig.class})
class ProfileWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private InterestService interestService;

    @MockitoBean
    private Authentication authentication;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private UserProfileDto testUserProfile;
    private Interest interest1;
    private Interest interest2;

    @BeforeEach
    void setUp() {
        testUserProfile = new UserProfileDto();
        testUserProfile.setId(1L);
        testUserProfile.setEmail("test@example.com");
        testUserProfile.setFirstName("John");
        testUserProfile.setLastName("Doe");
        testUserProfile.setGender(User.Gender.MALE);
        testUserProfile.setAge(25);
        testUserProfile.setCity("Warsaw");
        testUserProfile.setBio("Test bio");
        testUserProfile.setPhotoUrl("/uploads/test.jpg");
        testUserProfile.setCreatedAt(LocalDateTime.now());
        testUserProfile.setInterests(List.of("Sports", "Music"));

        interest1 = new Interest();
        interest1.setId(1L);
        interest1.setName("Sports");

        interest2 = new Interest();
        interest2.setId(2L);
        interest2.setName("Music");
    }

    @Test
    void testShowProfile_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserProfile);

        mockMvc.perform(get("/profile").with(
                        user("test@example.com").roles("USER")
                ))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", testUserProfile))
                .andExpect(model().attribute("user", hasProperty("firstName", is("John"))))
                .andExpect(model().attribute("user", hasProperty("lastName", is("Doe"))))
                .andExpect(model().attribute("user", hasProperty("city", is("Warsaw"))))
                .andExpect(model().attribute("user", hasProperty("bio", is("Test bio"))));

        verify(userService, times(1)).getUserByEmail("test@example.com");
    }

    @Test
    void testShowEditForm_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserProfile);
        when(interestService.getAllInterests()).thenReturn(List.of(interest1, interest2));

        mockMvc.perform(get("/profile/edit").with(
                        user("test@example.com").roles("USER")
                ))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-edit"))
                .andExpect(model().attributeExists("userUpdateDto"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allInterests"))
                .andExpect(model().attribute("user", testUserProfile))
                .andExpect(model().attribute("allInterests", hasSize(2)))
                .andExpect(model().attribute("userUpdateDto", hasProperty("city", is("Warsaw"))))
                .andExpect(model().attribute("userUpdateDto", hasProperty("bio", is("Test bio"))));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(interestService, times(1)).getAllInterests();
    }

    @Test
    void testUpdateProfile_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserProfile);
        when(userService.updateUserProfile(eq(1L), any(UserUpdateDto.class))).thenReturn(testUserProfile);

        mockMvc.perform(post("/profile/edit")
                        .with(csrf()).with(
                                user("test@example.com").roles("USER")
                        )
                        .param("bio", "Updated bio")
                        .param("city", "Krakow")
                        .param("interestIds", "1", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("successMessage", "Profile updated successfully!"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(userService, times(1)).updateUserProfile(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    void testUpdateProfile_ValidationFails_BioTooLong() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserProfile);

        String longBio = "a".repeat(1001);

        mockMvc.perform(post("/profile/edit")
                        .with(csrf()).with(
                                user("test@example.com").roles("USER")
                        )
                        .param("bio", longBio)
                        .param("city", "Warsaw"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-edit"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().hasErrors());

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    void testUpdateProfile_ValidationFails_CityTooLong() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserProfile);

        String longCity = "a".repeat(101);

        mockMvc.perform(post("/profile/edit")
                        .with(csrf()).with(
                                user("test@example.com").roles("USER")
                        )
                        .param("bio", "Valid bio")
                        .param("city", longCity))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-edit"))
                .andExpect(model().hasErrors());

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    void testUploadProfilePhoto_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserProfile);
        when(userService.uploadProfilePhoto(eq(1L), any())).thenReturn("/uploads/new-photo.jpg");

        mockMvc.perform(multipart("/profile/photo")
                        .file(file)
                        .with(csrf()).with(
                                user("test@example.com").roles("USER")
                        ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("successMessage", "Photo uploaded successfully!"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(userService, times(1)).uploadProfilePhoto(eq(1L), any());
    }

    @Test
    void testDeleteProfile_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserProfile);
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(post("/profile/delete")
                        .with(csrf())
                        .with(user("test@example.com").roles("USER"))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logout"))
                .andExpect(flash().attribute("successMessage", "Account deleted successfully!"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void testShowProfile_DisplaysUserAttributes() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserProfile);

        mockMvc.perform(get("/profile").with(
                        user("test@example.com").roles("USER")
                ))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", hasProperty("email", is("test@example.com"))))
                .andExpect(model().attribute("user", hasProperty("age", is(25))))
                .andExpect(model().attribute("user", hasProperty("gender", is(User.Gender.MALE))))
                .andExpect(model().attribute("user", hasProperty("photoUrl", is("/uploads/test.jpg"))))
                .andExpect(model().attribute("user", hasProperty("interests", hasSize(2))));
    }
}

