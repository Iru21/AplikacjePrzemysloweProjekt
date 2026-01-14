package me.iru.datingapp.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.iru.datingapp.dto.RatingDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.Rating;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.service.MatchingService;
import me.iru.datingapp.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchingController.class)
@Import(ObjectMapper.class)
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MatchingService matchingService;

    @MockitoBean
    private RatingService ratingService;

    private UserProfileDto suggestedUser1;
    private UserProfileDto suggestedUser2;
    private RatingDto ratingDto;

    @BeforeEach
    void setUp() {
        suggestedUser1 = new UserProfileDto();
        suggestedUser1.setId(2L);
        suggestedUser1.setEmail("user2@example.com");
        suggestedUser1.setFirstName("Jane");
        suggestedUser1.setLastName("Smith");
        suggestedUser1.setGender(User.Gender.FEMALE);
        suggestedUser1.setAge(23);
        suggestedUser1.setCity("Warsaw");
        suggestedUser1.setBio("Looking for friends");
        suggestedUser1.setCreatedAt(LocalDateTime.now());
        suggestedUser1.setInterests(List.of("Sports", "Travel"));

        suggestedUser2 = new UserProfileDto();
        suggestedUser2.setId(3L);
        suggestedUser2.setEmail("user3@example.com");
        suggestedUser2.setFirstName("Alice");
        suggestedUser2.setLastName("Johnson");
        suggestedUser2.setGender(User.Gender.FEMALE);
        suggestedUser2.setAge(26);
        suggestedUser2.setCity("Krakow");
        suggestedUser2.setCreatedAt(LocalDateTime.now());

        ratingDto = new RatingDto();
        ratingDto.setRatedUserId(2L);
        ratingDto.setRatingType(Rating.RatingType.LIKE);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetSuggestions_Success() throws Exception {
        List<UserProfileDto> suggestions = List.of(suggestedUser1, suggestedUser2);
        Page<UserProfileDto> suggestionsPage = new PageImpl<>(suggestions, PageRequest.of(0, 10), suggestions.size());

        when(matchingService.getSuggestedUsers(eq(1L), any(Pageable.class))).thenReturn(suggestionsPage);

        mockMvc.perform(get("/api/matching/suggestions")
                        .param("userId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(2)))
                .andExpect(jsonPath("$.content[0].firstName", is("Jane")))
                .andExpect(jsonPath("$.content[0].interests", hasSize(2)))
                .andExpect(jsonPath("$.content[1].id", is(3)))
                .andExpect(jsonPath("$.content[1].firstName", is("Alice")))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.size", is(10)));

        verify(matchingService, times(1)).getSuggestedUsers(eq(1L), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetSuggestions_EmptyList() throws Exception {
        Page<UserProfileDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(matchingService.getSuggestedUsers(eq(1L), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/matching/suggestions")
                        .param("userId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));

        verify(matchingService, times(1)).getSuggestedUsers(eq(1L), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetSuggestions_UserNotFound() throws Exception {
        when(matchingService.getSuggestedUsers(eq(999L), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/matching/suggestions")
                        .param("userId", "999")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());

        verify(matchingService, times(1)).getSuggestedUsers(eq(999L), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetSuggestions_WithCustomPagination() throws Exception {
        List<UserProfileDto> suggestions = List.of(suggestedUser1);
        Page<UserProfileDto> suggestionsPage = new PageImpl<>(suggestions, PageRequest.of(1, 5), 10);

        when(matchingService.getSuggestedUsers(eq(1L), any(Pageable.class))).thenReturn(suggestionsPage);

        mockMvc.perform(get("/api/matching/suggestions")
                        .param("userId", "1")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.number", is(1)))
                .andExpect(jsonPath("$.size", is(5)))
                .andExpect(jsonPath("$.totalElements", is(10)));

        verify(matchingService, times(1)).getSuggestedUsers(eq(1L), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testRateUser_Like_Success() throws Exception {
        doNothing().when(ratingService).rateUser(eq(1L), any(RatingDto.class));

        mockMvc.perform(post("/api/matching/rate")
                        .with(csrf())
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingDto)))
                .andExpect(status().isCreated());

        verify(ratingService, times(1)).rateUser(eq(1L), any(RatingDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testRateUser_Dislike_Success() throws Exception {
        ratingDto.setRatingType(Rating.RatingType.DISLIKE);
        doNothing().when(ratingService).rateUser(eq(1L), any(RatingDto.class));

        mockMvc.perform(post("/api/matching/rate")
                        .with(csrf())
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingDto)))
                .andExpect(status().isCreated());

        verify(ratingService, times(1)).rateUser(eq(1L), any(RatingDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testRateUser_ValidationFails_MissingRatedUserId() throws Exception {
        ratingDto.setRatedUserId(null);

        mockMvc.perform(post("/api/matching/rate")
                        .with(csrf())
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingDto)))
                .andExpect(status().isBadRequest());

        verify(ratingService, never()).rateUser(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testRateUser_ValidationFails_MissingRatingType() throws Exception {
        ratingDto.setRatingType(null);

        mockMvc.perform(post("/api/matching/rate")
                        .with(csrf())
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingDto)))
                .andExpect(status().isBadRequest());

        verify(ratingService, never()).rateUser(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testRateUser_RatedUserNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Rated user not found"))
                .when(ratingService).rateUser(eq(1L), any(RatingDto.class));

        mockMvc.perform(post("/api/matching/rate")
                        .with(csrf())
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingDto)))
                .andExpect(status().isNotFound());

        verify(ratingService, times(1)).rateUser(eq(1L), any(RatingDto.class));
    }
}

