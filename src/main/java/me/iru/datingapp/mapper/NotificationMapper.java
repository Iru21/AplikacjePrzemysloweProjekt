package me.iru.datingapp.mapper;

import me.iru.datingapp.dto.NotificationDto;
import me.iru.datingapp.entity.Notification;
import me.iru.datingapp.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setType(notification.getType());
        dto.setMessage(notification.getMessage());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setRelatedEntityId(notification.getRelatedEntityId());

        if (notification.getRelatedUser() != null) {
            User relatedUser = notification.getRelatedUser();
            dto.setRelatedUserId(relatedUser.getId());
            dto.setRelatedUserName(relatedUser.getFirstName() + " " + relatedUser.getLastName());
            dto.setRelatedUserPhotoUrl(relatedUser.getPhotoUrl());
        }

        return dto;
    }

    public List<NotificationDto> toDtoList(List<Notification> notifications) {
        if (notifications == null) {
            return null;
        }
        return notifications.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

