package me.iru.datingapp.controller.web;

import me.iru.datingapp.config.SecurityConfig;
import me.iru.datingapp.dto.MatchDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.service.MatchService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchWebController.class)
@Import({SecurityConfig.class})
class MatchWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchService matchService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private UserProfileDto currentUser;
    private List<MatchDto> matches;

    @BeforeEach
    void setUp() {
        currentUser = new UserProfileDto();
        currentUser.setId(1L);
        currentUser.setEmail("test@example.com");
        currentUser.setFirstName("John");
        currentUser.setLastName("Doe");
        currentUser.setGender(User.Gender.MALE);
        currentUser.setAge(25);

        MatchDto match1 = new MatchDto();
        match1.setId(1L);
        match1.setMatchedUserId(2L);
        match1.setMatchedUserName("Jane Smith");
        match1.setMatchedUserPhotoUrl("/uploads/jane.jpg");
        match1.setMatchedAt(LocalDateTime.now());
        match1.setIsActive(true);

        MatchDto match2 = new MatchDto();
        match2.setId(2L);
        match2.setMatchedUserId(3L);
        match2.setMatchedUserName("Alice Johnson");
        match2.setMatchedUserPhotoUrl("/uploads/alice.jpg");
        match2.setMatchedAt(LocalDateTime.now());
        match2.setIsActive(true);

        matches = Arrays.asList(match1, match2);
    }

    @Test
    void testShowMatches_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(matchService.getActiveMatches(1L)).thenReturn(matches);

        mockMvc.perform(get("/matches")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("matches"))
                .andExpect(model().attribute("matches", hasSize(2)))
                .andExpect(model().attribute("currentUserId", 1L))
                .andExpect(model().attribute("matches", hasItem(hasProperty("matchedUserName", is("Jane Smith")))))
                .andExpect(model().attribute("matches", hasItem(hasProperty("matchedUserName", is("Alice Johnson")))));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(matchService, times(1)).getActiveMatches(1L);
    }

    @Test
    void testShowMatches_EmptyList() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(matchService.getActiveMatches(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/matches")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("matches"))
                .andExpect(model().attribute("matches", hasSize(0)))
                .andExpect(model().attribute("currentUserId", 1L));

        verify(matchService, times(1)).getActiveMatches(1L);
    }

    @Test
    void testUnmatch_Success() throws Exception {
        Long matchId = 1L;

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        doNothing().when(matchService).unmatch(1L, matchId);

        mockMvc.perform(post("/matches/{matchId}/unmatch", matchId)
                        .with(csrf())
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/matches"))
                .andExpect(flash().attribute("successMessage", "Unmatched successfully!"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(matchService, times(1)).unmatch(1L, matchId);
    }

    @Test
    void testUnmatch_ServiceThrowsException() throws Exception {
        Long matchId = 1L;

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        doThrow(new RuntimeException("Match not found")).when(matchService).unmatch(1L, matchId);

        mockMvc.perform(post("/matches/{matchId}/unmatch", matchId)
                        .with(csrf())
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/matches"))
                .andExpect(flash().attribute("errorMessage", "Unmatch failed: Match not found"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(matchService, times(1)).unmatch(1L, matchId);
    }

    @Test
    void testShowMatches_VerifiesMatchProperties() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(matchService.getActiveMatches(1L)).thenReturn(matches);

        mockMvc.perform(get("/matches")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("matches", hasItem(allOf(
                        hasProperty("id", is(1L)),
                        hasProperty("matchedUserId", is(2L)),
                        hasProperty("isActive", is(true))
                ))))
                .andExpect(model().attribute("matches", hasItem(allOf(
                        hasProperty("id", is(2L)),
                        hasProperty("matchedUserId", is(3L)),
                        hasProperty("isActive", is(true))
                ))));

        verify(matchService, times(1)).getActiveMatches(1L);
    }

    @Test
    void testUnmatch_WithoutCsrf_Fails() throws Exception {
        mockMvc.perform(post("/matches/{matchId}/unmatch", 1L)
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isForbidden());

        verify(matchService, never()).unmatch(anyLong(), anyLong());
    }

}

