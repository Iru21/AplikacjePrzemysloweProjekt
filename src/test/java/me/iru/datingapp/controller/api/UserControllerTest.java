package me.iru.datingapp.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.dto.UserUpdateDto;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(ObjectMapper.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserProfileDto testUserProfile;
    private UserUpdateDto testUserUpdate;

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

        testUserUpdate = new UserUpdateDto();
        testUserUpdate.setBio("Updated bio");
        testUserUpdate.setCity("Krakow");
        testUserUpdate.setInterestIds(List.of(1L, 2L));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUserProfile);

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.gender", is("MALE")))
                .andExpect(jsonPath("$.age", is(25)))
                .andExpect(jsonPath("$.city", is("Warsaw")))
                .andExpect(jsonPath("$.bio", is("Test bio")))
                .andExpect(jsonPath("$.interests", hasSize(2)))
                .andExpect(jsonPath("$.interests[0]", is("Sports")))
                .andExpect(jsonPath("$.interests[1]", is("Music")));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(999L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(999L);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void testGetAllUsers_Success() throws Exception {
        UserProfileDto user2 = new UserProfileDto();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");

        List<UserProfileDto> users = List.of(testUserProfile, user2);
        Page<UserProfileDto> userPage = new PageImpl<>(users, PageRequest.of(0, 10), users.size());

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.size", is(10)));

        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testUpdateUser_Success() throws Exception {
        UserProfileDto updatedProfile = new UserProfileDto();
        updatedProfile.setId(1L);
        updatedProfile.setEmail("test@example.com");
        updatedProfile.setFirstName("John");
        updatedProfile.setLastName("Doe");
        updatedProfile.setGender(User.Gender.MALE);
        updatedProfile.setAge(25);
        updatedProfile.setCity("Krakow");
        updatedProfile.setBio("Updated bio");
        updatedProfile.setCreatedAt(LocalDateTime.now());

        when(userService.updateUserProfile(eq(1L), any(UserUpdateDto.class))).thenReturn(updatedProfile);

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.city", is("Krakow")))
                .andExpect(jsonPath("$.bio", is("Updated bio")));

        verify(userService, times(1)).updateUserProfile(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void testDeleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void testDeleteUser_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUser(999L);

        mockMvc.perform(delete("/api/users/{id}", 999L)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(999L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testUpdateUser_ValidationFails_BioTooLong() throws Exception {
        UserUpdateDto invalidUpdate = new UserUpdateDto();
        invalidUpdate.setBio("a".repeat(1001));
        invalidUpdate.setCity("Warsaw");

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testUpdateUser_ValidationFails_CityTooLong() throws Exception {
        UserUpdateDto invalidUpdate = new UserUpdateDto();
        invalidUpdate.setBio("Valid bio");
        invalidUpdate.setCity("a".repeat(101));

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testUploadProfilePhoto_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(userService.uploadProfilePhoto(eq(1L), any())).thenReturn("/uploads/test.jpg");

        mockMvc.perform(multipart("/api/users/{id}/photo", 1L)
                        .file(file)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string("/uploads/test.jpg"));

        verify(userService, times(1)).uploadProfilePhoto(eq(1L), any());
    }
}

