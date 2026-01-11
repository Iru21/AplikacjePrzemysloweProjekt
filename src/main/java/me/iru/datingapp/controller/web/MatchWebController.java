package me.iru.datingapp.controller.web;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.MatchDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.service.MatchService;
import me.iru.datingapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Web controller for viewing and managing matches
 */
@Controller
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchWebController {

    private static final Logger log = LoggerFactory.getLogger(MatchWebController.class);

    private final MatchService matchService;
    private final UserService userService;

    @GetMapping
    public String showMatches(Authentication authentication, Model model) {
        String email = authentication.getName();
        UserProfileDto currentUser = userService.getUserByEmail(email);

        List<MatchDto> matches = matchService.getActiveMatches(currentUser.getId());

        model.addAttribute("matches", matches);
        model.addAttribute("currentUserId", currentUser.getId());

        return "matches";
    }

    @PostMapping("/{matchId}/unmatch")
    public String unmatch(
            Authentication authentication,
            @PathVariable Long matchId,
            RedirectAttributes redirectAttributes) {

        try {
            String email = authentication.getName();
            UserProfileDto currentUser = userService.getUserByEmail(email);

            matchService.unmatch(currentUser.getId(), matchId);

            log.info("User {} unmatched from match {}", currentUser.getId(), matchId);
            redirectAttributes.addFlashAttribute("successMessage", "Unmatched successfully!");

            return "redirect:/matches";
        } catch (Exception e) {
            log.error("Unmatch failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Unmatch failed: " + e.getMessage());
            return "redirect:/matches";
        }
    }
}

