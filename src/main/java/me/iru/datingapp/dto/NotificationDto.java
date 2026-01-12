package me.iru.datingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.iru.datingapp.entity.Notification;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Long id;
    private Long userId;
    private Notification.NotificationType type;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private Long relatedUserId;
    private String relatedUserName;
    private String relatedUserPhotoUrl;
    private Long relatedEntityId;
}

