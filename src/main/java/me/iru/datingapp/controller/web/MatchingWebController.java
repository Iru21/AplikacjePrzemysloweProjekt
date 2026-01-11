package me.iru.datingapp.controller.web;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.RatingDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.Rating;
import me.iru.datingapp.service.MatchingService;
import me.iru.datingapp.service.RatingService;
import me.iru.datingapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingWebController {

    private static final Logger log = LoggerFactory.getLogger(MatchingWebController.class);

    private final MatchingService matchingService;
    private final RatingService ratingService;
    private final UserService userService;

    @GetMapping
    public String showMatching(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        String email = authentication.getName();
        UserProfileDto currentUser = userService.getUserByEmail(email);

        Page<UserProfileDto> suggestions = matchingService.getSuggestedUsers(
                currentUser.getId(),
                PageRequest.of(page, 10)
        );

        model.addAttribute("suggestions", suggestions.getContent());
        model.addAttribute("hasNext", suggestions.hasNext());
        model.addAttribute("currentPage", page);

        return "matching";
    }

    @PostMapping("/rate")
    public String rateUser(
            Authentication authentication,
            @RequestParam Long ratedUserId,
            @RequestParam String ratingType,
            @RequestParam(defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {

        try {
            String email = authentication.getName();
            UserProfileDto currentUser = userService.getUserByEmail(email);

            RatingDto ratingDto = new RatingDto();
            ratingDto.setRatedUserId(ratedUserId);
            ratingDto.setRatingType(Rating.RatingType.valueOf(ratingType));

            ratingService.rateUser(currentUser.getId(), ratingDto);

            log.info("User {} rated user {} with {}", currentUser.getId(), ratedUserId, ratingType);

            if (ratingType.equals("LIKE")) {
                redirectAttributes.addFlashAttribute("infoMessage", "User liked!");
            }

            return "redirect:/matching?page=" + page;
        } catch (Exception e) {
            log.error("Rating failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Rating failed: " + e.getMessage());
            return "redirect:/matching";
        }
    }
}

