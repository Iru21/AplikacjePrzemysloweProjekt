package me.iru.datingapp.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.iru.datingapp.dto.MessageDto;
import me.iru.datingapp.exception.MatchNotActiveException;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@Import(ObjectMapper.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageService messageService;

    private MessageDto messageDto;
    private MessageDto savedMessage;
    private MessageDto message1;
    private MessageDto message2;

    @BeforeEach
    void setUp() {
        messageDto = new MessageDto();
        messageDto.setContent("Hello, how are you?");
        messageDto.setSenderId(1L);
        messageDto.setReceiverId(2L);
        messageDto.setMatchId(1L);

        savedMessage = new MessageDto();
        savedMessage.setId(1L);
        savedMessage.setContent("Hello, how are you?");
        savedMessage.setSenderId(1L);
        savedMessage.setReceiverId(2L);
        savedMessage.setMatchId(1L);
        savedMessage.setSentAt(LocalDateTime.now());
        savedMessage.setIsRead(false);

        message1 = new MessageDto();
        message1.setId(1L);
        message1.setContent("Hello!");
        message1.setSenderId(1L);
        message1.setReceiverId(2L);
        message1.setMatchId(1L);
        message1.setSentAt(LocalDateTime.now().minusMinutes(10));
        message1.setIsRead(true);

        message2 = new MessageDto();
        message2.setId(2L);
        message2.setContent("Hi there!");
        message2.setSenderId(2L);
        message2.setReceiverId(1L);
        message2.setMatchId(1L);
        message2.setSentAt(LocalDateTime.now().minusMinutes(5));
        message2.setIsRead(true);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testSendMessage_Success() throws Exception {
        when(messageService.sendMessage(any(MessageDto.class))).thenReturn(savedMessage);

        mockMvc.perform(post("/api/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.content", is("Hello, how are you?")))
                .andExpect(jsonPath("$.senderId", is(1)))
                .andExpect(jsonPath("$.receiverId", is(2)))
                .andExpect(jsonPath("$.matchId", is(1)))
                .andExpect(jsonPath("$.isRead", is(false)));

        verify(messageService, times(1)).sendMessage(any(MessageDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testSendMessage_ValidationFails_EmptyContent() throws Exception {
        messageDto.setContent("");

        mockMvc.perform(post("/api/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDto)))
                .andExpect(status().isBadRequest());

        verify(messageService, never()).sendMessage(any());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testSendMessage_ValidationFails_NullContent() throws Exception {
        messageDto.setContent(null);

        mockMvc.perform(post("/api/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDto)))
                .andExpect(status().isBadRequest());

        verify(messageService, never()).sendMessage(any());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testSendMessage_ValidationFails_ContentTooLong() throws Exception {
        messageDto.setContent("a".repeat(5001));

        mockMvc.perform(post("/api/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDto)))
                .andExpect(status().isBadRequest());

        verify(messageService, never()).sendMessage(any());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testSendMessage_ValidationFails_MissingSenderId() throws Exception {
        messageDto.setSenderId(null);

        mockMvc.perform(post("/api/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDto)))
                .andExpect(status().isBadRequest());

        verify(messageService, never()).sendMessage(any());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testSendMessage_MatchNotActive() throws Exception {
        when(messageService.sendMessage(any(MessageDto.class)))
                .thenThrow(new MatchNotActiveException("Match is not active"));

        mockMvc.perform(post("/api/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDto)))
                .andExpect(status().isBadRequest());

        verify(messageService, times(1)).sendMessage(any(MessageDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testSendMessage_MatchNotFound() throws Exception {
        when(messageService.sendMessage(any(MessageDto.class)))
                .thenThrow(new ResourceNotFoundException("Match not found"));

        mockMvc.perform(post("/api/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDto)))
                .andExpect(status().isNotFound());

        verify(messageService, times(1)).sendMessage(any(MessageDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetMessageHistory_Success() throws Exception {
        List<MessageDto> messages = List.of(message1, message2);
        when(messageService.getMessageHistory(eq(1L), eq(1L))).thenReturn(messages);

        mockMvc.perform(get("/api/messages/match/{matchId}", 1L)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].content", is("Hello!")))
                .andExpect(jsonPath("$[0].senderId", is(1)))
                .andExpect(jsonPath("$[0].isRead", is(true)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].content", is("Hi there!")))
                .andExpect(jsonPath("$[1].senderId", is(2)));

        verify(messageService, times(1)).getMessageHistory(eq(1L), eq(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetMessageHistory_EmptyList() throws Exception {
        when(messageService.getMessageHistory(eq(1L), eq(1L))).thenReturn(List.of());

        mockMvc.perform(get("/api/messages/match/{matchId}", 1L)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(messageService, times(1)).getMessageHistory(eq(1L), eq(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetMessageHistory_MatchNotFound() throws Exception {
        when(messageService.getMessageHistory(eq(999L), eq(1L)))
                .thenThrow(new ResourceNotFoundException("Match not found"));

        mockMvc.perform(get("/api/messages/match/{matchId}", 999L)
                        .param("userId", "1"))
                .andExpect(status().isNotFound());

        verify(messageService, times(1)).getMessageHistory(eq(999L), eq(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testDeleteConversation_Success() throws Exception {
        doNothing().when(messageService).deleteConversation(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/messages/conversation/{matchId}", 1L)
                        .with(csrf())
                        .param("userId", "1"))
                .andExpect(status().isNoContent());

        verify(messageService, times(1)).deleteConversation(eq(1L), eq(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testDeleteConversation_MatchNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Match not found"))
                .when(messageService).deleteConversation(eq(1L), eq(999L));

        mockMvc.perform(delete("/api/messages/conversation/{matchId}", 999L)
                        .with(csrf())
                        .param("userId", "1"))
                .andExpect(status().isNotFound());

        verify(messageService, times(1)).deleteConversation(eq(1L), eq(999L));
    }

}

