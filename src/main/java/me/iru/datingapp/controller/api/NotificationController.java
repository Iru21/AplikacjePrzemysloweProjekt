package me.iru.datingapp.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.ErrorResponseDto;
import me.iru.datingapp.dto.NotificationDto;
import me.iru.datingapp.service.NotificationService;
import me.iru.datingapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Notifications", description = "Notification management endpoints")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;
    private final UserService userService;

    @Operation(summary = "Get user notifications", description = "Get paginated list of notifications for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getUserNotifications(
            Authentication authentication,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        log.info("Fetching notifications for user {} (page: {}, size: {})", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications = notificationService.getUserNotifications(userId, pageable);

        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread notifications", description = "Get all unread notifications for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully")
    })
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        log.info("Fetching unread notifications for user {}", userId);

        List<NotificationDto> notifications = notificationService.getUnreadNotifications(userId);

        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread count", description = "Get count of unread notifications for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully")
    })
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        log.info("Fetching unread notification count for user {}", userId);

        Long count = notificationService.getUnreadCount(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "Notification ID") @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        log.info("Marking notification {} as read for user {}", id, userId);

        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark all as read", description = "Mark all notifications as read for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All notifications marked as read")
    })
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        log.info("Marking all notifications as read for user {}", userId);

        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "Notification ID") @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        log.info("Deleting notification {} for user {}", id, userId);

        notificationService.deleteNotification(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all notifications", description = "Delete all notifications for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "All notifications deleted successfully")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        log.info("Deleting all notifications for user {}", userId);

        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.noContent().build();
    }
}

