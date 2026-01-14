package me.iru.datingapp.controller.api;

import me.iru.datingapp.config.SecurityConfig;
import me.iru.datingapp.dto.NotificationDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.Notification;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private Authentication authentication;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;


    private UserProfileDto testUser;
    private List<NotificationDto> notificationList;

    @BeforeEach
    void setUp() {
        testUser = new UserProfileDto();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");

        NotificationDto notification1 = new NotificationDto();
        notification1.setId(1L);
        notification1.setUserId(1L);
        notification1.setType(Notification.NotificationType.NEW_MATCH);
        notification1.setMessage("You have a new match!");
        notification1.setIsRead(false);
        notification1.setCreatedAt(LocalDateTime.now());

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
    void testGetUserNotifications_Success() throws Exception {
        Page<NotificationDto> page = new PageImpl<>(notificationList, PageRequest.of(0, 20), notificationList.size());

        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(notificationService.getUserNotifications(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/notifications")
                        .with(user("test@example.com").roles("USER"))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].message", is("You have a new match!")))
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.content[1].message", is("You have a new message!")));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(notificationService, times(1)).getUserNotifications(eq(1L), any(Pageable.class));
    }

    @Test
    void testGetUnreadNotifications_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(notificationService.getUnreadNotifications(1L)).thenReturn(notificationList);

        mockMvc.perform(get("/api/notifications/unread")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].isRead", is(false)))
                .andExpect(jsonPath("$[1].isRead", is(false)));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(notificationService, times(1)).getUnreadNotifications(1L);
    }

    @Test
    void testGetUnreadCount_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(notificationService.getUnreadCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/unread/count")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(5)));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(notificationService, times(1)).getUnreadCount(1L);
    }

    @Test
    void testMarkAsRead_Success() throws Exception {
        Long notificationId = 1L;

        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        doNothing().when(notificationService).markAsRead(notificationId, 1L);

        mockMvc.perform(put("/api/notifications/{id}/read", notificationId)
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(notificationService, times(1)).markAsRead(notificationId, 1L);
    }

    @Test
    void testMarkAllAsRead_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        doNothing().when(notificationService).markAllAsRead(1L);

        mockMvc.perform(put("/api/notifications/read-all")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(notificationService, times(1)).markAllAsRead(1L);
    }

    @Test
    void testDeleteNotification_Success() throws Exception {
        Long notificationId = 1L;

        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        doNothing().when(notificationService).deleteNotification(notificationId, 1L);

        mockMvc.perform(delete("/api/notifications/{id}", notificationId)
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(notificationService, times(1)).deleteNotification(notificationId, 1L);
    }

    @Test
    void testDeleteAllNotifications_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        doNothing().when(notificationService).deleteAllNotifications(1L);

        mockMvc.perform(delete("/api/notifications").with(user("test@example.com").roles("USER")))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(notificationService, times(1)).deleteAllNotifications(1L);
    }
}

