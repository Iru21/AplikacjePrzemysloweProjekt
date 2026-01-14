package me.iru.datingapp.controller.api;

import me.iru.datingapp.entity.Interest;
import me.iru.datingapp.service.InterestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InterestController.class)
@AutoConfigureMockMvc
class InterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterestService interestService;

    private List<Interest> interestList;

    @BeforeEach
    void setUp() {
        Interest interest1 = new Interest();
        interest1.setId(1L);
        interest1.setName("Sports");

        Interest interest2 = new Interest();
        interest2.setId(2L);
        interest2.setName("Music");

        interestList = Arrays.asList(interest1, interest2);
    }

    @Test
    void testGetAllInterests_Success() throws Exception {
        when(interestService.getAllInterests()).thenReturn(interestList);

        mockMvc.perform(get("/api/interests")
                        .with(httpBasic("test@example.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Sports")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Music")));

        verify(interestService, times(1)).getAllInterests();
    }

    @Test
    void testGetAllInterests_EmptyList() throws Exception {
        when(interestService.getAllInterests()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/interests")
                        .with(httpBasic("test@example.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(interestService, times(1)).getAllInterests();
    }

    @Test
    void testAddInterestToUser_Success() throws Exception {
        Long userId = 1L;
        Long interestId = 1L;

        doNothing().when(interestService).addInterestToUser(userId, interestId);

        mockMvc.perform(post("/api/interests/users/{userId}/interests/{interestId}", userId, interestId)
                        .with(httpBasic("test@example.com", "password")))
                .andExpect(status().isCreated());

        verify(interestService, times(1)).addInterestToUser(userId, interestId);
    }

    @Test
    void testRemoveInterestFromUser_Success() throws Exception {
        Long userId = 1L;
        Long interestId = 1L;

        doNothing().when(interestService).removeInterestFromUser(userId, interestId);

        mockMvc.perform(delete("/api/interests/users/{userId}/interests/{interestId}", userId, interestId)
                        .with(httpBasic("test@example.com", "password")))
                .andExpect(status().isNoContent());

        verify(interestService, times(1)).removeInterestFromUser(userId, interestId);
    }
}

