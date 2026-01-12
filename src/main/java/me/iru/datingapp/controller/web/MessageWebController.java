package me.iru.datingapp.controller.web;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.MatchDto;
import me.iru.datingapp.dto.MessageDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.service.MatchService;
import me.iru.datingapp.service.MessageService;
import me.iru.datingapp.service.NotificationService;
import me.iru.datingapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageWebController {

    private static final Logger log = LoggerFactory.getLogger(MessageWebController.class);

    private final MessageService messageService;
    private final UserService userService;
    private final MatchService matchService;
    private final NotificationService notificationService;

    @GetMapping("/{matchId}")
    public String showChat(
            Authentication authentication,
            @PathVariable Long matchId,
            Model model) {

        String email = authentication.getName();
        UserProfileDto currentUser = userService.getUserByEmail(email);

        MatchDto match = matchService.getMatchById(matchId, currentUser.getId());
        Long receiverId = match.getMatchedUserId();

        List<MessageDto> messages = messageService.getMessageHistory(matchId, currentUser.getId());

        model.addAttribute("messages", messages);
        model.addAttribute("matchId", matchId);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("receiverId", receiverId);
        model.addAttribute("newMessage", new MessageDto());

        return "chat";
    }

    @PostMapping
    public String sendMessage(
            Authentication authentication,
            @RequestParam Long matchId,
            @RequestParam Long receiverId,
            @RequestParam String content,
            RedirectAttributes redirectAttributes) {

        try {
            String email = authentication.getName();
            UserProfileDto currentUser = userService.getUserByEmail(email);

            MessageDto messageDto = new MessageDto();
            messageDto.setMatchId(matchId);
            messageDto.setSenderId(currentUser.getId());
            messageDto.setReceiverId(receiverId);
            messageDto.setContent(content);

            messageService.sendMessage(messageDto);

            notificationService.createMessageNotification(receiverId, currentUser, matchId);

            log.info("Message sent from user {} to user {}", currentUser.getId(), receiverId);

            return "redirect:/messages/" + matchId;
        } catch (Exception e) {
            log.error("Message sending failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send message: " + e.getMessage());
            return "redirect:/messages/" + matchId;
        }
    }
}

