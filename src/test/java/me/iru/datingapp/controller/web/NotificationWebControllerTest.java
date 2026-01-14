package me.iru.datingapp.controller.web;

import me.iru.datingapp.config.SecurityConfig;
import me.iru.datingapp.dto.NotificationDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.Notification;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.service.NotificationService;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationWebController.class)
@Import({SecurityConfig.class})
class NotificationWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private UserProfileDto currentUser;
    private List<NotificationDto> notificationList;

    @BeforeEach
    void setUp() {
        currentUser = new UserProfileDto();
        currentUser.setId(1L);
        currentUser.setEmail("test@example.com");
        currentUser.setFirstName("John");
        currentUser.setLastName("Doe");
        currentUser.setGender(User.Gender.MALE);
        currentUser.setAge(25);

        NotificationDto notification1 = new NotificationDto();
        notification1.setId(1L);
        notification1.setUserId(1L);
        notification1.setType(Notification.NotificationType.NEW_MATCH);
        notification1.setMessage("You have a new match!");
        notification1.setIsRead(false);
        notification1.setCreatedAt(LocalDateTime.now().minusHours(1));

        NotificationDto notification2 = new NotificationDto();
        notification2.setId(2L);
        notification2.setUserId(1L);
        notification2.setType(Notification.NotificationType.NEW_MESSAGE);
        notification2.setMessage("You have a new message!");
        notification2.setIsRead(false);
        notification2.setCreatedAt(LocalDateTime.now());

        notificationList = Arrays.asList(notification1, notification2);
    }

    @Test
    void testGetNotifications_Success() throws Exception {
        Page<NotificationDto> page = new PageImpl<>(notificationList, PageRequest.of(0, 20), notificationList.size());

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(notificationService.getUserNotifications(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/notifications")
                        .with(user("test@example.com").roles("USER"))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().attribute("currentPage", 0));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(notificationService, times(1)).getUserNotifications(eq(1L), any(Pageable.class));
    }

    @Test
    void testGetNotifications_WithPagination() throws Exception {
        Page<NotificationDto> page = new PageImpl<>(notificationList, PageRequest.of(2, 10), 25);

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(notificationService.getUserNotifications(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/notifications")
                        .with(user("test@example.com").roles("USER"))
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attribute("currentPage", 2));

        verify(notificationService, times(1)).getUserNotifications(eq(1L), any(Pageable.class));
    }

    @Test
    void testMarkAsRead_Success() throws Exception {
        Long notificationId = 1L;

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        doNothing().when(notificationService).markAsRead(notificationId, 1L);

        mockMvc.perform(post("/notifications/{id}/read", notificationId)
                        .with(csrf())
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(notificationService, times(1)).markAsRead(notificationId, 1L);
    }

    @Test
    void testMarkAsRead_WithRedirectParameter() throws Exception {
        Long notificationId = 1L;
        String redirectUrl = "/matches";

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        doNothing().when(notificationService).markAsRead(notificationId, 1L);

        mockMvc.perform(post("/notifications/{id}/read", notificationId)
                        .with(csrf())
                        .with(user("test@example.com").roles("USER"))
                        .param("redirect", redirectUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(redirectUrl));

        verify(notificationService, times(1)).markAsRead(notificationId, 1L);
    }

    @Test
    void testMarkAllAsRead_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        doNothing().when(notificationService).markAllAsRead(1L);

        mockMvc.perform(post("/notifications/read-all")
                        .with(csrf())
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(notificationService, times(1)).markAllAsRead(1L);
    }

    @Test
    void testDeleteNotification_Success() throws Exception {
        Long notificationId = 1L;

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        doNothing().when(notificationService).deleteNotification(notificationId, 1L);

        mockMvc.perform(post("/notifications/{id}/delete", notificationId)
                        .with(csrf())
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(notificationService, times(1)).deleteNotification(notificationId, 1L);
    }

    @Test
    void testGetNotifications_DefaultParameters() throws Exception {
        Page<NotificationDto> page = new PageImpl<>(notificationList, PageRequest.of(0, 20), notificationList.size());

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(notificationService.getUserNotifications(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/notifications")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attribute("currentPage", 0));

        verify(notificationService, times(1)).getUserNotifications(eq(1L), any(Pageable.class));
    }

    @Test
    void testGetNotifications_EmptyList() throws Exception {
        Page<NotificationDto> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(notificationService.getUserNotifications(eq(1L), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/notifications")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("notifications"));

        verify(notificationService, times(1)).getUserNotifications(eq(1L), any(Pageable.class));
    }

    @Test
    void testMarkAsRead_WithoutCsrf_Fails() throws Exception {
        mockMvc.perform(post("/notifications/{id}/read", 1L)
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isForbidden());

        verify(notificationService, never()).markAsRead(anyLong(), anyLong());
    }

    @Test
    void testMarkAllAsRead_WithoutCsrf_Fails() throws Exception {
        mockMvc.perform(post("/notifications/read-all")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isForbidden());

        verify(notificationService, never()).markAllAsRead(anyLong());
    }
}

