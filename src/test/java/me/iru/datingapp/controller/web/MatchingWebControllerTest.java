package me.iru.datingapp.controller.web;

import me.iru.datingapp.config.SecurityConfig;
import me.iru.datingapp.dto.RatingDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.service.MatchingService;
import me.iru.datingapp.service.RatingService;
import me.iru.datingapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchingWebController.class)
@Import({SecurityConfig.class})
class MatchingWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchingService matchingService;

    @MockitoBean
    private RatingService ratingService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private UserProfileDto currentUser;
    private List<UserProfileDto> suggestions;

    @BeforeEach
    void setUp() {
        currentUser = new UserProfileDto();
        currentUser.setId(1L);
        currentUser.setEmail("test@example.com");
        currentUser.setFirstName("John");
        currentUser.setLastName("Doe");
        currentUser.setGender(User.Gender.MALE);
        currentUser.setAge(25);
        currentUser.setCity("Warsaw");

        UserProfileDto suggestedUser1 = new UserProfileDto();
        suggestedUser1.setId(2L);
        suggestedUser1.setEmail("jane@example.com");
        suggestedUser1.setFirstName("Jane");
        suggestedUser1.setLastName("Smith");
        suggestedUser1.setGender(User.Gender.FEMALE);
        suggestedUser1.setAge(23);
        suggestedUser1.setCity("Warsaw");

        UserProfileDto suggestedUser2 = new UserProfileDto();
        suggestedUser2.setId(3L);
        suggestedUser2.setEmail("alice@example.com");
        suggestedUser2.setFirstName("Alice");
        suggestedUser2.setLastName("Johnson");
        suggestedUser2.setGender(User.Gender.FEMALE);
        suggestedUser2.setAge(24);
        suggestedUser2.setCity("Warsaw");

        suggestions = Arrays.asList(suggestedUser1, suggestedUser2);
    }

    @Test
    void testShowMatching_Success() throws Exception {
        Page<UserProfileDto> page = new PageImpl<>(suggestions, PageRequest.of(0, 10), suggestions.size());

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(matchingService.getSuggestedUsers(eq(1L), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/matching")
                        .with(user("test@example.com").roles("USER"))
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("matching"))
                .andExpect(model().attribute("suggestions", hasSize(2)))
                .andExpect(model().attribute("hasNext", false))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("suggestions", hasItem(hasProperty("firstName", is("Jane")))))
                .andExpect(model().attribute("suggestions", hasItem(hasProperty("firstName", is("Alice")))));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(matchingService, times(1)).getSuggestedUsers(eq(1L), any(PageRequest.class));
    }

    @Test
    void testShowMatching_WithPagination() throws Exception {
        Page<UserProfileDto> page = new PageImpl<>(suggestions, PageRequest.of(1, 10), 15);

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(matchingService.getSuggestedUsers(eq(1L), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/matching")
                        .with(user("test@example.com").roles("USER"))
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("matching"))
                .andExpect(model().attribute("hasNext", false))
                .andExpect(model().attribute("currentPage", 1));

        verify(matchingService, times(1)).getSuggestedUsers(eq(1L), any(PageRequest.class));
    }

    @Test
    void testRateUser_Like_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        doNothing().when(ratingService).rateUser(eq(1L), any(RatingDto.class));

        mockMvc.perform(post("/matching/rate")
                        .with(csrf())
                        .with(user("test@example.com").roles("USER"))
                        .param("ratedUserId", "2")
                        .param("ratingType", "LIKE")
                        .param("page", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/matching?page=0"))
                .andExpect(flash().attribute("infoMessage", "User liked!"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(ratingService, times(1)).rateUser(eq(1L), any(RatingDto.class));
    }

    @Test
    void testRateUser_Dislike_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        doNothing().when(ratingService).rateUser(eq(1L), any(RatingDto.class));

        mockMvc.perform(post("/matching/rate")
                        .with(csrf())
                        .with(user("test@example.com").roles("USER"))
                        .param("ratedUserId", "2")
                        .param("ratingType", "DISLIKE")
                        .param("page", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/matching?page=0"))
                .andExpect(flash().attributeCount(0));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(ratingService, times(1)).rateUser(eq(1L), any(RatingDto.class));
    }

    @Test
    void testRateUser_WithPageParameter() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        doNothing().when(ratingService).rateUser(eq(1L), any(RatingDto.class));

        mockMvc.perform(post("/matching/rate")
                        .with(csrf())
                        .with(user("test@example.com").roles("USER"))
                        .param("ratedUserId", "2")
                        .param("ratingType", "LIKE")
                        .param("page", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/matching?page=2"));

        verify(ratingService, times(1)).rateUser(eq(1L), any(RatingDto.class));
    }

    @Test
    void testRateUser_ServiceThrowsException() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        doThrow(new RuntimeException("Already rated")).when(ratingService).rateUser(eq(1L), any(RatingDto.class));

        mockMvc.perform(post("/matching/rate")
                        .with(csrf())
                        .with(user("test@example.com").roles("USER"))
                        .param("ratedUserId", "2")
                        .param("ratingType", "LIKE")
                        .param("page", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/matching"))
                .andExpect(flash().attribute("errorMessage", "Rating failed: Already rated"));

        verify(ratingService, times(1)).rateUser(eq(1L), any(RatingDto.class));
    }

    @Test
    void testShowMatching_EmptySuggestions() throws Exception {
        Page<UserProfileDto> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(matchingService.getSuggestedUsers(eq(1L), any(PageRequest.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/matching")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("matching"))
                .andExpect(model().attribute("suggestions", hasSize(0)))
                .andExpect(model().attribute("hasNext", false));

        verify(matchingService, times(1)).getSuggestedUsers(eq(1L), any(PageRequest.class));
    }
}

