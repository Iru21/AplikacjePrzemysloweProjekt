package me.iru.datingapp.controller.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.dto.UserUpdateDto;
import me.iru.datingapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileWebController {

    private static final Logger log = LoggerFactory.getLogger(ProfileWebController.class);

    private final UserService userService;

    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        String email = authentication.getName();
        UserProfileDto profile = userService.getUserByEmail(email);
        model.addAttribute("user", profile);
        return "profile";
    }

    @GetMapping("/edit")
    public String showEditForm(Authentication authentication, Model model) {
        String email = authentication.getName();
        UserProfileDto profile = userService.getUserByEmail(email);

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setCity(profile.getCity());
        updateDto.setBio(profile.getBio());

        model.addAttribute("userUpdateDto", updateDto);
        model.addAttribute("user", profile);
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String updateProfile(
            Authentication authentication,
            @Valid @ModelAttribute("userUpdateDto") UserUpdateDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String email = authentication.getName();
            UserProfileDto profile = userService.getUserByEmail(email);
            model.addAttribute("user", profile);
            return "profile-edit";
        }

        try {
            String email = authentication.getName();
            UserProfileDto profile = userService.getUserByEmail(email);
            userService.updateUserProfile(profile.getId(), dto);

            log.info("Profile updated successfully for user: {}", email);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/profile";
        } catch (Exception e) {
            log.error("Profile update failed: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "profile-edit";
        }
    }

    @PostMapping("/photo")
    public String uploadPhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        try {
            String email = authentication.getName();
            UserProfileDto profile = userService.getUserByEmail(email);

            userService.uploadProfilePhoto(profile.getId(), file);

            log.info("Photo uploaded successfully for user: {}", email);
            redirectAttributes.addFlashAttribute("successMessage", "Photo uploaded successfully!");
        } catch (Exception e) {
            log.error("Photo upload failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Photo upload failed: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/delete")
    public String deleteAccount(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UserProfileDto profile = userService.getUserByEmail(email);

            userService.deleteUser(profile.getId());

            log.info("Account deleted successfully for user: {}", email);
            redirectAttributes.addFlashAttribute("successMessage", "Account deleted successfully!");
            return "redirect:/logout";
        } catch (Exception e) {
            log.error("Account deletion failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Account deletion failed: " + e.getMessage());
            return "redirect:/profile";
        }
    }
}

