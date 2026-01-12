package me.iru.datingapp.service;

import me.iru.datingapp.dto.NotificationDto;
import me.iru.datingapp.entity.Notification;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.mapper.NotificationMapper;
import me.iru.datingapp.repository.NotificationRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private User relatedUser;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setGender(User.Gender.MALE);
        user.setAge(25);

        relatedUser = new User();
        relatedUser.setId(2L);
        relatedUser.setEmail("related@example.com");
        relatedUser.setFirstName("Jane");
        relatedUser.setLastName("Smith");
        relatedUser.setGender(User.Gender.FEMALE);
        relatedUser.setAge(23);

        notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);
        notification.setType(Notification.NotificationType.NEW_MATCH);
        notification.setMessage("You have a new match!");
        notification.setIsRead(false);
        notification.setRelatedUser(relatedUser);
    }

    @Test
    void createNotification_shouldCreateNotificationSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(relatedUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.createNotification(1L, Notification.NotificationType.NEW_MATCH,
                "Test message", 2L, 123L);

        verify(userRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.createNotification(1L,
                Notification.NotificationType.NEW_MATCH, "Test message", 2L, 123L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 1");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void createNotification_shouldHandleNullRelatedUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.createNotification(1L, Notification.NotificationType.SYSTEM,
                "System message", null, null);

        verify(userRepository).findById(1L);
        verify(userRepository, never()).findById(2L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createMatchNotification_shouldCreateNotificationWithCorrectMessage() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.createMatchNotification(1L, relatedUser, 123L);

        verify(userRepository).findById(1L);
        verify(notificationRepository).save(argThat(n ->
                n.getType() == Notification.NotificationType.NEW_MATCH &&
                n.getMessage().contains("Jane Smith") &&
                n.getRelatedEntityId().equals(123L)
        ));
    }

    @Test
    void createMessageNotification_shouldCreateNotificationWithCorrectMessage() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.createMessageNotification(1L, relatedUser, 456L);

        verify(userRepository).findById(1L);
        verify(notificationRepository).save(argThat(n ->
                n.getType() == Notification.NotificationType.NEW_MESSAGE &&
                n.getMessage().contains("Jane Smith") &&
                n.getRelatedEntityId().equals(456L)
        ));
    }

    @Test
    void getUserNotifications_shouldReturnPageOfNotifications() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(Collections.singletonList(notification));

        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setId(1L);
        notificationDto.setUserId(1L);
        notificationDto.setType(Notification.NotificationType.NEW_MATCH);
        notificationDto.setMessage("You have a new match!");
        notificationDto.setIsRead(false);

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);
        when(notificationMapper.toDto(notification)).thenReturn(notificationDto);

        Page<NotificationDto> result = notificationService.getUserNotifications(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(1L, pageable);
    }

    @Test
    void getUnreadNotifications_shouldReturnUnreadNotifications() {
        List<Notification> unreadList = Collections.singletonList(notification);
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setId(1L);
        notificationDto.setUserId(1L);
        notificationDto.setType(Notification.NotificationType.NEW_MATCH);
        notificationDto.setMessage("You have a new match!");
        notificationDto.setIsRead(false);
        List<NotificationDto> dtoList = Collections.singletonList(notificationDto);

        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(unreadList);
        when(notificationMapper.toDtoList(unreadList)).thenReturn(dtoList);

        List<NotificationDto> result = notificationService.getUnreadNotifications(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(notificationRepository).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L);
        verify(notificationMapper).toDtoList(unreadList);
    }

    @Test
    void getUnreadCount_shouldReturnCorrectCount() {
        when(notificationRepository.countUnreadByUserId(1L)).thenReturn(5L);

        Long count = notificationService.getUnreadCount(1L);

        assertThat(count).isEqualTo(5L);
        verify(notificationRepository).countUnreadByUserId(1L);
    }

    @Test
    void markAsRead_shouldMarkNotificationAsRead() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsRead(1L, 1L);

        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(argThat(n -> n.getIsRead() == true));
    }

    @Test
    void markAsRead_shouldThrowExceptionWhenNotificationNotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification not found with ID: 1");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAsRead_shouldThrowExceptionWhenUnauthorized() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification not found with ID: 1");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAllAsRead_shouldMarkAllNotificationsAsRead() {
        notificationService.markAllAsRead(1L);

        verify(notificationRepository).markAllAsReadByUserId(1L);
    }

    @Test
    void deleteNotification_shouldDeleteNotificationSuccessfully() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(1L, 1L);

        verify(notificationRepository).findById(1L);
        verify(notificationRepository).delete(notification);
    }

    @Test
    void deleteNotification_shouldThrowExceptionWhenNotificationNotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.deleteNotification(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification not found with ID: 1");

        verify(notificationRepository, never()).delete(any(Notification.class));
    }

    @Test
    void deleteNotification_shouldThrowExceptionWhenUnauthorized() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.deleteNotification(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification not found with ID: 1");

        verify(notificationRepository, never()).delete(any(Notification.class));
    }

    @Test
    void deleteAllNotifications_shouldDeleteAllUserNotifications() {
        notificationService.deleteAllNotifications(1L);

        verify(notificationRepository).deleteByUserId(1L);
    }
}

