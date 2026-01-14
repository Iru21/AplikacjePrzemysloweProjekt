package me.iru.datingapp.controller.web;

import me.iru.datingapp.config.SecurityConfig;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.Interest;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.service.AdminService;
import me.iru.datingapp.service.InterestService;
import me.iru.datingapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private InterestService interestService;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private UserProfileDto testUser;
    private User user1;
    private User user2;
    private Interest interest1;
    private Interest interest2;

    @BeforeEach
    void setUp() {
        testUser = new UserProfileDto();
        testUser.setId(1L);
        testUser.setEmail("admin@example.com");
        testUser.setFirstName("Admin");
        testUser.setLastName("User");

        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setGender(User.Gender.MALE);
        user1.setAge(25);

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setGender(User.Gender.FEMALE);
        user2.setAge(23);

        interest1 = new Interest();
        interest1.setId(1L);
        interest1.setName("Sports");

        interest2 = new Interest();
        interest2.setId(2L);
        interest2.setName("Music");
    }

    @Test
    void testAdminDashboard_Success() throws Exception {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", 100L);
        stats.put("totalMatches", 50L);
        stats.put("totalMessages", 200L);
        stats.put("totalRatings", 150L);

        when(adminService.getPlatformStatistics()).thenReturn(stats);

        mockMvc.perform(get("/admin")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("totalUsers", 100L))
                .andExpect(model().attribute("totalMatches", 50L))
                .andExpect(model().attribute("totalMessages", 200L))
                .andExpect(model().attribute("totalRatings", 150L));

        verify(adminService, times(1)).getPlatformStatistics();
    }

    @Test
    void testListUsers_Success() throws Exception {
        List<User> users = Arrays.asList(user1, user2);
        when(adminService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/admin/users")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attribute("users", hasSize(2)))
                .andExpect(model().attribute("users", hasItem(user1)))
                .andExpect(model().attribute("users", hasItem(user2)));

        verify(adminService, times(1)).getAllUsers();
    }

    @Test
    void testViewUser_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUser);

        mockMvc.perform(get("/admin/users/{id}", 1L)
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-detail"))
                .andExpect(model().attribute("user", testUser));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(post("/admin/users/{id}/delete", 1L)
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("successMessage", "User deleted successfully!"));

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void testListInterests_Success() throws Exception {
        List<Interest> interests = Arrays.asList(interest1, interest2);
        when(interestService.getAllInterests()).thenReturn(interests);

        mockMvc.perform(get("/admin/interests")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/interests"))
                .andExpect(model().attribute("interests", hasSize(2)))
                .andExpect(model().attributeExists("newInterest"));

        verify(interestService, times(1)).getAllInterests();
    }

    @Test
    void testCreateInterest_Success() throws Exception {
        when(interestService.createInterest(any(Interest.class))).thenReturn(interest1);

        mockMvc.perform(post("/admin/interests")
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN"))
                        .param("name", "Sports"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/interests"))
                .andExpect(flash().attribute("successMessage", "Interest created successfully!"));

        verify(interestService, times(1)).createInterest(any(Interest.class));
    }

    @Test
    void testDeleteInterest_Success() throws Exception {
        doNothing().when(interestService).deleteInterest(1L);

        mockMvc.perform(post("/admin/interests/{id}/delete", 1L)
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/interests"))
                .andExpect(flash().attribute("successMessage", "Interest deleted successfully!"));

        verify(interestService, times(1)).deleteInterest(1L);
    }

    @Test
    void testEditInterestForm_Success() throws Exception {
        when(interestService.getInterestById(1L)).thenReturn(interest1);

        mockMvc.perform(get("/admin/interests/{id}/edit", 1L)
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/interest-edit"))
                .andExpect(model().attribute("interest", interest1));

        verify(interestService, times(1)).getInterestById(1L);
    }

    @Test
    void testUpdateInterest_Success() throws Exception {
        when(interestService.updateInterest(eq(1L), any(Interest.class))).thenReturn(interest1);

        mockMvc.perform(post("/admin/interests/{id}/edit", 1L)
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN"))
                        .param("name", "Updated Sports"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/interests"))
                .andExpect(flash().attribute("successMessage", "Interest updated successfully!"));

        verify(interestService, times(1)).updateInterest(eq(1L), any(Interest.class));
    }

    @Test
    void testStatistics_Success() throws Exception {
        Map<String, Long> userStats = new HashMap<>();
        userStats.put("totalUsers", 100L);
        userStats.put("maleUsers", 60L);
        userStats.put("femaleUsers", 35L);
        userStats.put("otherGenderUsers", 5L);

        Map<String, Long> matchStats = new HashMap<>();
        matchStats.put("totalMatches", 50L);
        matchStats.put("activeMatches", 40L);

        Map<String, Long> activityStats = new HashMap<>();
        activityStats.put("totalMessages", 200L);
        activityStats.put("totalRatings", 150L);

        when(adminService.getUserStatistics()).thenReturn(userStats);
        when(adminService.getMatchStatistics()).thenReturn(matchStats);
        when(adminService.getActivityStatistics()).thenReturn(activityStats);

        mockMvc.perform(get("/admin/statistics")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/statistics"))
                .andExpect(model().attribute("totalUsers", 100L))
                .andExpect(model().attribute("maleUsers", 60L))
                .andExpect(model().attribute("femaleUsers", 35L))
                .andExpect(model().attribute("totalMatches", 50L))
                .andExpect(model().attribute("activeMatches", 40L))
                .andExpect(model().attribute("totalMessages", 200L))
                .andExpect(model().attribute("totalRatings", 150L));

        verify(adminService, times(1)).getUserStatistics();
        verify(adminService, times(1)).getMatchStatistics();
        verify(adminService, times(1)).getActivityStatistics();
    }

    @Test
    void testAdminDashboard_AccessDeniedForNonAdmin() throws Exception {
        mockMvc.perform(get("/admin")
                        .with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());

        verify(adminService, never()).getPlatformStatistics();
    }
}

