package me.iru.datingapp.controller.web;

import me.iru.datingapp.config.SecurityConfig;
import me.iru.datingapp.dto.MatchDto;
import me.iru.datingapp.dto.MessageDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.service.MatchService;
import me.iru.datingapp.service.MessageService;
import me.iru.datingapp.service.NotificationService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageWebController.class)
@Import({SecurityConfig.class})
class MessageWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MatchService matchService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private UserProfileDto currentUser;
    private MatchDto match;
    private MessageDto message1;
    private List<MessageDto> messages;

    @BeforeEach
    void setUp() {
        currentUser = new UserProfileDto();
        currentUser.setId(1L);
        currentUser.setEmail("test@example.com");
        currentUser.setFirstName("John");
        currentUser.setLastName("Doe");
        currentUser.setGender(User.Gender.MALE);
        currentUser.setAge(25);

        match = new MatchDto();
        match.setId(1L);
        match.setMatchedUserId(2L);
        match.setMatchedUserName("Jane Smith");
        match.setMatchedUserPhotoUrl("/uploads/jane.jpg");
        match.setIsActive(true);

        message1 = new MessageDto();
        message1.setId(1L);
        message1.setMatchId(1L);
        message1.setSenderId(1L);
        message1.setReceiverId(2L);
        message1.setContent("Hello!");
        message1.setSentAt(LocalDateTime.now().minusHours(1));

        MessageDto message2 = new MessageDto();
        message2.setId(2L);
        message2.setMatchId(1L);
        message2.setSenderId(2L);
        message2.setReceiverId(1L);
        message2.setContent("Hi there!");
        message2.setSentAt(LocalDateTime.now());

        messages = Arrays.asList(message1, message2);
    }

    @Test
    void testShowChat_Success() throws Exception {
        Long matchId = 1L;

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(matchService.getMatchById(matchId, 1L)).thenReturn(match);
        when(messageService.getMessageHistory(matchId, 1L)).thenReturn(messages);

        mockMvc.perform(get("/messages/{matchId}", matchId)
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"))
                .andExpect(model().attribute("messages", hasSize(2)))
                .andExpect(model().attribute("matchId", matchId))
                .andExpect(model().attribute("currentUserId", 1L))
                .andExpect(model().attribute("receiverId", 2L))
                .andExpect(model().attributeExists("newMessage"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(matchService, times(1)).getMatchById(matchId, 1L);
        verify(messageService, times(1)).getMessageHistory(matchId, 1L);
    }

    @Test
    void testShowChat_EmptyMessageHistory() throws Exception {
        Long matchId = 1L;

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(matchService.getMatchById(matchId, 1L)).thenReturn(match);
        when(messageService.getMessageHistory(matchId, 1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/messages/{matchId}", matchId)
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"))
                .andExpect(model().attribute("messages", hasSize(0)));

        verify(messageService, times(1)).getMessageHistory(matchId, 1L);
    }

    @Test
    void testSendMessage_Success() throws Exception {
        Long matchId = 1L;
        Long receiverId = 2L;
        String content = "Hello, how are you?";

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(messageService.sendMessage(any(MessageDto.class))).thenReturn(message1);
        doNothing().when(notificationService).createMessageNotification(eq(receiverId), any(UserProfileDto.class), eq(matchId));

        mockMvc.perform(post("/messages")
                        .with(csrf())
                        .with(user("test@example.com").roles("USER"))
                        .param("matchId", matchId.toString())
                        .param("receiverId", receiverId.toString())
                        .param("content", content))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messages/" + matchId));

        verify(userService, times(1)).getUserByEmail("test@example.com");
        verify(messageService, times(1)).sendMessage(any(MessageDto.class));
        verify(notificationService, times(1)).createMessageNotification(eq(receiverId), any(UserProfileDto.class), eq(matchId));
    }

    @Test
    void testSendMessage_ServiceThrowsException() throws Exception {
        long matchId = 1L;
        long receiverId = 2L;

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(messageService.sendMessage(any(MessageDto.class)))
                .thenThrow(new RuntimeException("Failed to send message"));

        mockMvc.perform(post("/messages")
                        .with(csrf())
                        .with(user("test@example.com").roles("USER"))
                        .param("matchId", Long.toString(matchId))
                        .param("receiverId", Long.toString(receiverId))
                        .param("content", "Hello"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messages/" + matchId))
                .andExpect(flash().attribute("errorMessage", "Failed to send message: Failed to send message"));

        verify(messageService, times(1)).sendMessage(any(MessageDto.class));
        verify(notificationService, never()).createMessageNotification(anyLong(), any(UserProfileDto.class), anyLong());
    }

    @Test
    void testShowChat_VerifyMessageContent() throws Exception {
        Long matchId = 1L;

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(matchService.getMatchById(matchId, 1L)).thenReturn(match);
        when(messageService.getMessageHistory(matchId, 1L)).thenReturn(messages);

        mockMvc.perform(get("/messages/{matchId}", matchId)
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("messages", hasItem(hasProperty("content", is("Hello!")))))
                .andExpect(model().attribute("messages", hasItem(hasProperty("content", is("Hi there!")))));

        verify(messageService, times(1)).getMessageHistory(matchId, 1L);
    }

    @Test
    void testSendMessage_WithoutCsrf_Fails() throws Exception {
        mockMvc.perform(post("/messages")
                        .with(user("test@example.com").roles("USER"))
                        .param("matchId", "1")
                        .param("receiverId", "2")
                        .param("content", "Hello"))
                .andExpect(status().isForbidden());

        verify(messageService, never()).sendMessage(any(MessageDto.class));
    }

    @Test
    void testSendMessage_VerifiesMessageDto() throws Exception {
        Long matchId = 1L;
        Long receiverId = 2L;
        String content = "Test message";

        when(userService.getUserByEmail("test@example.com")).thenReturn(currentUser);
        when(messageService.sendMessage(argThat(dto ->
                dto.getMatchId().equals(matchId) &&
                        dto.getSenderId().equals(1L) &&
                        dto.getReceiverId().equals(receiverId) &&
                        dto.getContent().equals(content)
        ))).thenReturn(message1);

        mockMvc.perform(post("/messages")
                        .with(csrf())
                        .with(user("test@example.com").roles("USER"))
                        .param("matchId", matchId.toString())
                        .param("receiverId", receiverId.toString())
                        .param("content", content))
                .andExpect(status().is3xxRedirection());

        verify(messageService, times(1)).sendMessage(any(MessageDto.class));
    }
}

