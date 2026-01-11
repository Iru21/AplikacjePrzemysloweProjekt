package me.iru.datingapp.service;

import me.iru.datingapp.dto.MessageDto;
import me.iru.datingapp.entity.Match;
import me.iru.datingapp.entity.Message;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.MatchNotActiveException;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.mapper.MessageMapper;
import me.iru.datingapp.repository.MatchRepository;
import me.iru.datingapp.repository.MessageRepository;
import me.iru.datingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageService messageService;

    private User sender;
    private User receiver;
    private Match match;
    private MessageDto messageDto;
    private Message message;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setEmail("sender@example.com");

        receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");

        match = new Match();
        match.setId(1L);
        match.setUser1(sender);
        match.setUser2(receiver);
        match.setIsActive(true);

        messageDto = new MessageDto();
        messageDto.setSenderId(1L);
        messageDto.setReceiverId(2L);
        messageDto.setMatchId(1L);
        messageDto.setContent("Hello!");

        message = new Message();
        message.setId(1L);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setMatch(match);
        message.setContent("Hello!");
    }

    @Test
    void testSendMessage_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(messageMapper.toEntity(any(MessageDto.class), eq(sender), eq(receiver), eq(match)))
                .thenReturn(message);
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(messageMapper.toDto(any(Message.class))).thenReturn(messageDto);

        MessageDto result = messageService.sendMessage(messageDto);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Hello!");
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testSendMessage_SenderNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage(messageDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sender not found");

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void testSendMessage_ReceiverNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage(messageDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Receiver not found");

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void testSendMessage_MatchNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(matchRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage(messageDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Match not found");

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void testSendMessage_MatchNotActive() {
        match.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> messageService.sendMessage(messageDto))
                .isInstanceOf(MatchNotActiveException.class)
                .hasMessageContaining("match is not active");

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void testSendMessage_UsersNotInMatch() {
        User otherUser = new User();
        otherUser.setId(3L);
        match.setUser2(otherUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> messageService.sendMessage(messageDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not part of this match");

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void testGetMessageHistory_Success() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(messageRepository.findByMatchIdOrderBySentAtAsc(1L)).thenReturn(List.of(message));
        when(messageMapper.toDto(any(Message.class))).thenReturn(messageDto);

        List<MessageDto> result = messageService.getMessageHistory(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(messageRepository).findByMatchIdOrderBySentAtAsc(1L);
    }

    @Test
    void testGetMessageHistory_MatchNotFound() {
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getMessageHistory(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Match not found");
    }

    @Test
    void testGetMessageHistory_UserNotInMatch() {
        User otherUser = new User();
        otherUser.setId(3L);
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> messageService.getMessageHistory(1L, 3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("access denied");
    }

    @Test
    void testGetUnreadMessageCount_Success() {
        when(messageRepository.findUnreadMessagesByUserId(2L))
                .thenReturn(List.of(message, message, message, message, message));

        long result = messageService.getUnreadMessageCount(2L);

        assertThat(result).isEqualTo(5L);
        verify(messageRepository).findUnreadMessagesByUserId(2L);
    }

    @Test
    void testMarkAsRead_Success() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        messageService.markAsRead(1L, 2L);

        verify(messageRepository).save(argThat(Message::getIsRead));
    }

    @Test
    void testMarkAsRead_MessageNotFound() {
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.markAsRead(999L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testMarkAsRead_UserNotReceiver() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> messageService.markAsRead(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("access denied");

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void testGetMessageHistory_EmptyList() {
        // Given
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(messageRepository.findByMatchIdOrderBySentAtAsc(1L)).thenReturn(List.of());

        // When
        List<MessageDto> result = messageService.getMessageHistory(1L, 1L);

        // Then
        assertThat(result).isEmpty();
    }
}

