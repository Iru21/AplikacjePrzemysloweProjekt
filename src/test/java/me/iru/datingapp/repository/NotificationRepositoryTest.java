package me.iru.datingapp.repository;

import me.iru.datingapp.entity.Notification;
import me.iru.datingapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private Notification notification1;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        user1.setFirstName("User");
        user1.setLastName("One");
        user1.setGender(User.Gender.MALE);
        user1.setAge(25);
        user1.setCity("Warsaw");
        user1 = entityManager.persistAndFlush(user1);

        user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setGender(User.Gender.FEMALE);
        user2.setAge(23);
        user2.setCity("Krakow");
        user2 = entityManager.persistAndFlush(user2);

        notification1 = new Notification();
        notification1.setUser(user1);
        notification1.setType(Notification.NotificationType.NEW_MATCH);
        notification1.setMessage("You have a new match!");
        notification1.setIsRead(false);
        notification1.setRelatedUser(user2);
        notification1 = entityManager.persistAndFlush(notification1);

        Notification notification2 = new Notification();
        notification2.setUser(user1);
        notification2.setType(Notification.NotificationType.NEW_MESSAGE);
        notification2.setMessage("You have a new message!");
        notification2.setIsRead(true);
        notification2.setRelatedUser(user2);
        entityManager.persistAndFlush(notification2);

        Notification notification3 = new Notification();
        notification3.setUser(user2);
        notification3.setType(Notification.NotificationType.NEW_MATCH);
        notification3.setMessage("You have a new match!");
        notification3.setIsRead(false);
        notification3.setRelatedUser(user1);
        entityManager.persistAndFlush(notification3);
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_shouldReturnUserNotifications() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user1.getId(), pageable);

        assertThat(notifications).isNotNull();
        assertThat(notifications.getContent()).hasSize(2);
        assertThat(notifications.getContent()).extracting("user").extracting("id")
                .containsOnly(user1.getId());
    }

    @Test
    void findByUserIdAndIsReadFalseOrderByCreatedAtDesc_shouldReturnUnreadNotifications() {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user1.getId());

        assertThat(unreadNotifications).isNotNull();
        assertThat(unreadNotifications).hasSize(1);
        assertThat(unreadNotifications.getFirst().getIsRead()).isFalse();
        assertThat(unreadNotifications.getFirst().getUser().getId()).isEqualTo(user1.getId());
    }

    @Test
    void countUnreadByUserId_shouldReturnCorrectCount() {
        Long count = notificationRepository.countUnreadByUserId(user1.getId());

        assertThat(count).isEqualTo(1L);
    }

    @Test
    void countUnreadByUserId_shouldReturnZeroWhenNoUnreadNotifications() {
        notificationRepository.markAllAsReadByUserId(user1.getId());
        entityManager.flush();
        entityManager.clear();

        Long count = notificationRepository.countUnreadByUserId(user1.getId());

        assertThat(count).isEqualTo(0L);
    }

    @Test
    void markAllAsReadByUserId_shouldMarkAllNotificationsAsRead() {
        notificationRepository.markAllAsReadByUserId(user1.getId());
        entityManager.flush();
        entityManager.clear();

        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user1.getId());

        assertThat(unreadNotifications).isEmpty();
    }

    @Test
    void deleteByUserId_shouldDeleteAllUserNotifications() {
        notificationRepository.deleteByUserId(user1.getId());
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user1.getId(), pageable);

        assertThat(notifications.getContent()).isEmpty();
    }

    @Test
    void findByUserIdAndType_shouldReturnNotificationsOfSpecificType() {
        List<Notification> matchNotifications = notificationRepository
                .findByUserIdAndType(user1.getId(), Notification.NotificationType.NEW_MATCH);

        assertThat(matchNotifications).isNotNull();
        assertThat(matchNotifications).hasSize(1);
        assertThat(matchNotifications.getFirst().getType()).isEqualTo(Notification.NotificationType.NEW_MATCH);
    }

    @Test
    void findByUserIdAndType_shouldReturnEmptyListWhenNoMatchingType() {
        List<Notification> systemNotifications = notificationRepository
                .findByUserIdAndType(user1.getId(), Notification.NotificationType.SYSTEM);

        assertThat(systemNotifications).isEmpty();
    }

    @Test
    void notification_shouldHaveRelatedUser() {
        Notification notification = notificationRepository.findById(notification1.getId()).orElseThrow();

        assertThat(notification.getRelatedUser()).isNotNull();
        assertThat(notification.getRelatedUser().getId()).isEqualTo(user2.getId());
    }

    @Test
    void notification_shouldHaveRelatedEntityId() {
        notification1.setRelatedEntityId(123L);
        entityManager.persistAndFlush(notification1);
        entityManager.clear();

        Notification notification = notificationRepository.findById(notification1.getId()).orElseThrow();

        assertThat(notification.getRelatedEntityId()).isEqualTo(123L);
    }

    @Test
    void notification_shouldBeCreatedWithDefaultIsReadFalse() {
        Notification newNotification = new Notification();
        newNotification.setUser(user1);
        newNotification.setType(Notification.NotificationType.SYSTEM);
        newNotification.setMessage("Test notification");

        Notification saved = notificationRepository.save(newNotification);
        entityManager.flush();
        entityManager.clear();

        Notification found = notificationRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getIsRead()).isFalse();
        assertThat(found.getCreatedAt()).isNotNull();
    }
}

