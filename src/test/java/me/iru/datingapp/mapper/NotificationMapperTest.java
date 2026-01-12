package me.iru.datingapp.mapper;

import me.iru.datingapp.dto.NotificationDto;
import me.iru.datingapp.entity.Notification;
import me.iru.datingapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMapperTest {

    private NotificationMapper notificationMapper;
    private User user;
    private User relatedUser;

    @BeforeEach
    void setUp() {
        notificationMapper = new NotificationMapper();

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
        relatedUser.setPhotoUrl("/uploads/photo.jpg");
    }

    @Test
    void toDto_shouldMapNotificationToDto() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);
        notification.setType(Notification.NotificationType.NEW_MATCH);
        notification.setMessage("You have a new match!");
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRelatedUser(relatedUser);
        notification.setRelatedEntityId(123L);

        NotificationDto dto = notificationMapper.toDto(notification);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getType()).isEqualTo(Notification.NotificationType.NEW_MATCH);
        assertThat(dto.getMessage()).isEqualTo("You have a new match!");
        assertThat(dto.getIsRead()).isFalse();
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getRelatedUserId()).isEqualTo(2L);
        assertThat(dto.getRelatedUserName()).isEqualTo("Jane Smith");
        assertThat(dto.getRelatedUserPhotoUrl()).isEqualTo("/uploads/photo.jpg");
        assertThat(dto.getRelatedEntityId()).isEqualTo(123L);
    }

    @Test
    void toDto_shouldHandleNullNotification() {
        NotificationDto dto = notificationMapper.toDto(null);

        assertThat(dto).isNull();
    }

    @Test
    void toDto_shouldHandleNotificationWithoutRelatedUser() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);
        notification.setType(Notification.NotificationType.SYSTEM);
        notification.setMessage("System notification");
        notification.setIsRead(true);
        notification.setCreatedAt(LocalDateTime.now());

        NotificationDto dto = notificationMapper.toDto(notification);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getRelatedUserId()).isNull();
        assertThat(dto.getRelatedUserName()).isNull();
        assertThat(dto.getRelatedUserPhotoUrl()).isNull();
    }

    @Test
    void toDtoList_shouldMapListOfNotifications() {
        Notification notification1 = new Notification();
        notification1.setId(1L);
        notification1.setUser(user);
        notification1.setType(Notification.NotificationType.NEW_MATCH);
        notification1.setMessage("Match 1");
        notification1.setIsRead(false);
        notification1.setCreatedAt(LocalDateTime.now());

        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setUser(user);
        notification2.setType(Notification.NotificationType.NEW_MESSAGE);
        notification2.setMessage("Message 1");
        notification2.setIsRead(true);
        notification2.setCreatedAt(LocalDateTime.now());

        List<Notification> notifications = Arrays.asList(notification1, notification2);
        List<NotificationDto> dtos = notificationMapper.toDtoList(notifications);

        assertThat(dtos).isNotNull();
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getId()).isEqualTo(1L);
        assertThat(dtos.get(0).getMessage()).isEqualTo("Match 1");
        assertThat(dtos.get(1).getId()).isEqualTo(2L);
        assertThat(dtos.get(1).getMessage()).isEqualTo("Message 1");
    }

    @Test
    void toDtoList_shouldHandleNullList() {
        List<NotificationDto> dtos = notificationMapper.toDtoList(null);

        assertThat(dtos).isNull();
    }

    @Test
    void toDtoList_shouldHandleEmptyList() {
        List<NotificationDto> dtos = notificationMapper.toDtoList(List.of());

        assertThat(dtos).isNotNull();
        assertThat(dtos).isEmpty();
    }

    @Test
    void toDto_shouldMapAllNotificationTypes() {
        Notification.NotificationType[] types = Notification.NotificationType.values();

        for (Notification.NotificationType type : types) {
            Notification notification = new Notification();
            notification.setId(1L);
            notification.setUser(user);
            notification.setType(type);
            notification.setMessage("Test message");
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());

            NotificationDto dto = notificationMapper.toDto(notification);

            assertThat(dto.getType()).isEqualTo(type);
        }
    }
}

