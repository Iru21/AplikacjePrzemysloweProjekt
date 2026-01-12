package me.iru.datingapp.controller.web;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.NotificationDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.service.NotificationService;
import me.iru.datingapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationWebController {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebController.class);

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public String getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication,
            Model model) {

        String email = authentication.getName();
        UserProfileDto currentUser = userService.getUserByEmail(email);
        Long userId = currentUser.getId();
        log.info("Web: Fetching notifications for user {} (page: {}, size: {})", userId , page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications = notificationService.getUserNotifications(userId , pageable);

        model.addAttribute("notifications", notifications);
        model.addAttribute("currentPage", page);

        return "notifications";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam(required = false) String redirect) {

        String email = authentication.getName();
        UserProfileDto currentUser = userService.getUserByEmail(email);
        Long userId = currentUser.getId();
        log.info("Web: Marking notification {} as read for user {}", id, userId);

        notificationService.markAsRead(id, userId);

        if (redirect != null && !redirect.isEmpty()) {
            return "redirect:" + redirect;
        }
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead(Authentication authentication) {
        String email = authentication.getName();
        UserProfileDto currentUser = userService.getUserByEmail(email);
        Long userId = currentUser.getId();
        log.info("Web: Marking all notifications as read for user {}", userId);

        notificationService.markAllAsRead(userId);
        return "redirect:/notifications";
    }

    @PostMapping("/{id}/delete")
    public String deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        UserProfileDto currentUser = userService.getUserByEmail(email);
        Long userId = currentUser.getId();
        log.info("Web: Deleting notification {} for user {}", id, userId);

        notificationService.deleteNotification(id, userId);
        return "redirect:/notifications";
    }
}

