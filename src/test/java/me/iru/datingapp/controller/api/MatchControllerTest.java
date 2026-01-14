package me.iru.datingapp.controller.api;

import me.iru.datingapp.dto.MatchDto;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchController.class)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchService matchService;


    private MatchDto match1;
    private MatchDto match2;

    @BeforeEach
    void setUp() {
        match1 = new MatchDto();
        match1.setId(1L);
        match1.setMatchedUserId(2L);
        match1.setMatchedUserName("Jane Smith");
        match1.setMatchedUserPhotoUrl("/uploads/jane.jpg");
        match1.setMatchedUserAge(23);
        match1.setMatchedUserCity("Warsaw");
        match1.setMatchedAt(LocalDateTime.now());
        match1.setIsActive(true);

        match2 = new MatchDto();
        match2.setId(2L);
        match2.setMatchedUserId(3L);
        match2.setMatchedUserName("Alice Johnson");
        match2.setMatchedUserPhotoUrl("/uploads/alice.jpg");
        match2.setMatchedUserAge(26);
        match2.setMatchedUserCity("Krakow");
        match2.setMatchedAt(LocalDateTime.now());
        match2.setIsActive(true);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetMatches_Success() throws Exception {
        List<MatchDto> matches = List.of(match1, match2);
        when(matchService.getActiveMatches(1L)).thenReturn(matches);

        mockMvc.perform(get("/api/matches")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].matchedUserId", is(2)))
                .andExpect(jsonPath("$[0].matchedUserName", is("Jane Smith")))
                .andExpect(jsonPath("$[0].isActive", is(true)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].matchedUserName", is("Alice Johnson")));

        verify(matchService, times(1)).getActiveMatches(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetMatches_EmptyList() throws Exception {
        when(matchService.getActiveMatches(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/matches")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(matchService, times(1)).getActiveMatches(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetMatches_UserNotFound() throws Exception {
        when(matchService.getActiveMatches(999L))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/matches")
                        .param("userId", "999"))
                .andExpect(status().isNotFound());

        verify(matchService, times(1)).getActiveMatches(999L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testUnmatch_Success() throws Exception {
        doNothing().when(matchService).unmatch(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/matches/{matchId}/unmatch", 1L)
                        .with(csrf())
                        .param("userId", "1"))
                .andExpect(status().isNoContent());

        verify(matchService, times(1)).unmatch(eq(1L), eq(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testUnmatch_MatchNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Match not found"))
                .when(matchService).unmatch(eq(1L), eq(999L));

        mockMvc.perform(delete("/api/matches/{matchId}/unmatch", 999L)
                        .with(csrf())
                        .param("userId", "1"))
                .andExpect(status().isNotFound());

        verify(matchService, times(1)).unmatch(eq(1L), eq(999L));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testUnmatch_UserNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found"))
                .when(matchService).unmatch(eq(999L), eq(1L));

        mockMvc.perform(delete("/api/matches/{matchId}/unmatch", 1L)
                        .with(csrf())
                        .param("userId", "999"))
                .andExpect(status().isNotFound());

        verify(matchService, times(1)).unmatch(eq(999L), eq(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testUnmatch_DeletesMessagesAutomatically() throws Exception {
        doNothing().when(matchService).unmatch(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/matches/{matchId}/unmatch", 1L)
                        .with(csrf())
                        .param("userId", "1"))
                .andExpect(status().isNoContent());

        verify(matchService, times(1)).unmatch(eq(1L), eq(1L));
    }
}

