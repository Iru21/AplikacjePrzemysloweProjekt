package me.iru.datingapp.controller.web;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.Interest;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.service.AdminService;
import me.iru.datingapp.service.InterestService;
import me.iru.datingapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final InterestService interestService;
    private final AdminService adminService;

    @GetMapping
    public String adminDashboard(Model model) {
        Map<String, Long> stats = adminService.getPlatformStatistics();

        model.addAttribute("totalUsers", stats.get("totalUsers"));
        model.addAttribute("totalMatches", stats.get("totalMatches"));
        model.addAttribute("totalMessages", stats.get("totalMessages"));
        model.addAttribute("totalRatings", stats.get("totalRatings"));

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = adminService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        UserProfileDto user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/user-detail";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            log.info("Admin deleted user with ID: {}", id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            log.error("Failed to delete user: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/interests")
    public String listInterests(Model model) {
        List<Interest> interests = interestService.getAllInterests();
        model.addAttribute("interests", interests);
        model.addAttribute("newInterest", new Interest());
        return "admin/interests";
    }

    @PostMapping("/interests")
    public String createInterest(@ModelAttribute Interest interest, RedirectAttributes redirectAttributes) {
        try {
            interestService.createInterest(interest);
            log.info("Admin created new interest: {}", interest.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Interest created successfully!");
        } catch (Exception e) {
            log.error("Failed to create interest: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create interest: " + e.getMessage());
        }
        return "redirect:/admin/interests";
    }

    @PostMapping("/interests/{id}/delete")
    public String deleteInterest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            interestService.deleteInterest(id);
            log.info("Admin deleted interest with ID: {}", id);
            redirectAttributes.addFlashAttribute("successMessage", "Interest deleted successfully!");
        } catch (Exception e) {
            log.error("Failed to delete interest: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete interest: " + e.getMessage());
        }
        return "redirect:/admin/interests";
    }

    @GetMapping("/interests/{id}/edit")
    public String editInterestForm(@PathVariable Long id, Model model) {
        Interest interest = interestService.getInterestById(id);
        model.addAttribute("interest", interest);
        return "admin/interest-edit";
    }

    @PostMapping("/interests/{id}/edit")
    public String updateInterest(@PathVariable Long id, @ModelAttribute Interest interest, RedirectAttributes redirectAttributes) {
        try {
            interestService.updateInterest(id, interest);
            log.info("Admin updated interest with ID: {}", id);
            redirectAttributes.addFlashAttribute("successMessage", "Interest updated successfully!");
        } catch (Exception e) {
            log.error("Failed to update interest: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update interest: " + e.getMessage());
        }
        return "redirect:/admin/interests";
    }


    @GetMapping("/statistics")
    public String statistics(Model model) {
        Map<String, Long> userStats = adminService.getUserStatistics();
        Map<String, Long> matchStats = adminService.getMatchStatistics();
        Map<String, Long> activityStats = adminService.getActivityStatistics();

        model.addAttribute("totalUsers", userStats.get("totalUsers"));
        model.addAttribute("maleUsers", userStats.get("maleUsers"));
        model.addAttribute("femaleUsers", userStats.get("femaleUsers"));
        model.addAttribute("otherGenderUsers", userStats.get("otherGenderUsers"));

        model.addAttribute("totalMatches", matchStats.get("totalMatches"));
        model.addAttribute("activeMatches", matchStats.get("activeMatches"));

        model.addAttribute("totalMessages", activityStats.get("totalMessages"));
        model.addAttribute("totalRatings", activityStats.get("totalRatings"));

        return "admin/statistics";
    }
}

