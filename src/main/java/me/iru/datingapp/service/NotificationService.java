package me.iru.datingapp.service;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.NotificationDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.Notification;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.mapper.NotificationMapper;
import me.iru.datingapp.repository.NotificationRepository;
import me.iru.datingapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public void createNotification(Long userId, Notification.NotificationType type, String message,
                                    Long relatedUserId, Long relatedEntityId) {
        log.info("Creating notification for user {} of type {}", userId, type);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setRelatedEntityId(relatedEntityId);

        if (relatedUserId != null) {
            User relatedUser = userRepository.findById(relatedUserId)
                    .orElse(null);
            notification.setRelatedUser(relatedUser);
        }

        notificationRepository.save(notification);
        log.info("Notification created successfully for user {}", userId);
    }

    public void createMatchNotification(Long userId, User matchedUser, Long matchId) {
        String message = String.format("You have a new match with %s %s!",
                matchedUser.getFirstName(), matchedUser.getLastName());
        createNotification(userId, Notification.NotificationType.NEW_MATCH, message,
                matchedUser.getId(), matchId);
    }

    public void createMessageNotification(Long userId, User sender, Long messageId) {
        String message = String.format("%s %s sent you a message",
                sender.getFirstName(), sender.getLastName());
        createNotification(userId, Notification.NotificationType.NEW_MESSAGE, message,
                sender.getId(), messageId);
    }

    public void createMessageNotification(Long userId, UserProfileDto sender, Long messageId) {
        String message = String.format("%s %s sent you a message",
                sender.getFirstName(), sender.getLastName());
        createNotification(userId, Notification.NotificationType.NEW_MESSAGE, message,
                sender.getId(), messageId);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching notifications for user {}", userId);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(notificationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        log.debug("Fetching unread notifications for user {}", userId);
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notificationMapper.toDtoList(notifications);
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        log.debug("Counting unread notifications for user {}", userId);
        return notificationRepository.countUnreadByUserId(userId);
    }

    public void markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification {} as read for user {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.error("Notification not found with ID: {}", notificationId);
                    return new ResourceNotFoundException("Notification not found with ID: " + notificationId);
                });

        if (!notification.getUser().getId().equals(userId)) {
            log.error("User {} is not authorized to mark notification {} as read", userId, notificationId);
            throw new ResourceNotFoundException("Notification not found with ID: " + notificationId);
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        log.info("Notification {} marked as read", notificationId);
    }

    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user {}", userId);
        notificationRepository.markAllAsReadByUserId(userId);
        log.info("All notifications marked as read for user {}", userId);
    }

    public void deleteNotification(Long notificationId, Long userId) {
        log.info("Deleting notification {} for user {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.error("Notification not found with ID: {}", notificationId);
                    return new ResourceNotFoundException("Notification not found with ID: " + notificationId);
                });

        if (!notification.getUser().getId().equals(userId)) {
            log.error("User {} is not authorized to delete notification {}", userId, notificationId);
            throw new ResourceNotFoundException("Notification not found with ID: " + notificationId);
        }

        notificationRepository.delete(notification);
        log.info("Notification {} deleted", notificationId);
    }

    public void deleteAllNotifications(Long userId) {
        log.info("Deleting all notifications for user {}", userId);
        notificationRepository.deleteByUserId(userId);
        log.info("All notifications deleted for user {}", userId);
    }
}

