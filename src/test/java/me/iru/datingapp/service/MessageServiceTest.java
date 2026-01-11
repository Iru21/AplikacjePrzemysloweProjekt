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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    void testDeleteConversation_Success() {
        Message message1 = new Message();
        message1.setId(1L);
        message1.setSender(sender);
        message1.setReceiver(receiver);
        message1.setMatch(match);
        message1.setContent("Message 1");

        Message message2 = new Message();
        message2.setId(2L);
        message2.setSender(receiver);
        message2.setReceiver(sender);
        message2.setMatch(match);
        message2.setContent("Message 2");

        List<Message> messages = List.of(message1, message2);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(messageRepository.findByMatchIdOrderBySentAtAsc(1L)).thenReturn(messages);

        messageService.deleteConversation(1L, 1L);

        verify(matchRepository).findById(1L);
        verify(messageRepository).findByMatchIdOrderBySentAtAsc(1L);
        verify(messageRepository).deleteAll(messages);
    }

    @Test
    void testDeleteConversation_MatchNotFound() {
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.deleteConversation(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Match not found");

        verify(matchRepository).findById(999L);
        verify(messageRepository, never()).findByMatchIdOrderBySentAtAsc(anyLong());
        verify(messageRepository, never()).deleteAll(anyList());
    }

    @Test
    void testDeleteConversation_UserNotInMatch() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> messageService.deleteConversation(3L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("access denied");

        verify(matchRepository).findById(1L);
        verify(messageRepository, never()).findByMatchIdOrderBySentAtAsc(anyLong());
        verify(messageRepository, never()).deleteAll(anyList());
    }

    @Test
    void testDeleteConversation_EmptyConversation() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(messageRepository.findByMatchIdOrderBySentAtAsc(1L)).thenReturn(List.of());

        messageService.deleteConversation(1L, 1L);

        verify(matchRepository).findById(1L);
        verify(messageRepository).findByMatchIdOrderBySentAtAsc(1L);
        verify(messageRepository).deleteAll(List.of());
    }

    @Test
    void testDeleteConversation_User2CanDelete() {
        List<Message> messages = List.of(message);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(messageRepository.findByMatchIdOrderBySentAtAsc(1L)).thenReturn(messages);

        messageService.deleteConversation(2L, 1L);

        verify(matchRepository).findById(1L);
        verify(messageRepository).deleteAll(messages);
    }


    @Test
    void testGetMessageHistoryPaginated_Success() {
        Message message1 = new Message();
        message1.setId(1L);
        message1.setSender(sender);
        message1.setReceiver(receiver);
        message1.setMatch(match);
        message1.setContent("Message 1");

        Message message2 = new Message();
        message2.setId(2L);
        message2.setSender(receiver);
        message2.setReceiver(sender);
        message2.setMatch(match);
        message2.setContent("Message 2");

        List<Message> messages = List.of(message1, message2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> messagePage = new PageImpl<>(messages, pageable, messages.size());

        MessageDto dto1 = new MessageDto();
        dto1.setId(1L);
        dto1.setContent("Message 1");

        MessageDto dto2 = new MessageDto();
        dto2.setId(2L);
        dto2.setContent("Message 2");

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(messageRepository.findByMatchId(1L, pageable)).thenReturn(messagePage);
        when(messageMapper.toDto(message1)).thenReturn(dto1);
        when(messageMapper.toDto(message2)).thenReturn(dto2);

        Page<MessageDto> result = messageService.getMessageHistoryPaginated(1L, 1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Message 1");
        assertThat(result.getContent().get(1).getContent()).isEqualTo("Message 2");
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(matchRepository).findById(1L);
        verify(messageRepository).findByMatchId(1L, pageable);
    }

    @Test
    void testGetMessageHistoryPaginated_MatchNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getMessageHistoryPaginated(999L, 1L, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Match not found");

        verify(matchRepository).findById(999L);
        verify(messageRepository, never()).findByMatchId(anyLong(), any(Pageable.class));
    }

    @Test
    void testGetMessageHistoryPaginated_UserNotInMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> messageService.getMessageHistoryPaginated(1L, 3L, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("access denied");

        verify(matchRepository).findById(1L);
        verify(messageRepository, never()).findByMatchId(anyLong(), any(Pageable.class));
    }

    @Test
    void testGetMessageHistoryPaginated_EmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(messageRepository.findByMatchId(1L, pageable)).thenReturn(emptyPage);

        Page<MessageDto> result = messageService.getMessageHistoryPaginated(1L, 1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(matchRepository).findById(1L);
        verify(messageRepository).findByMatchId(1L, pageable);
    }

    @Test
    void testGetMessageHistoryPaginated_User2CanAccess() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> messagePage = new PageImpl<>(List.of(message), pageable, 1);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(messageRepository.findByMatchId(1L, pageable)).thenReturn(messagePage);
        when(messageMapper.toDto(message)).thenReturn(messageDto);

        Page<MessageDto> result = messageService.getMessageHistoryPaginated(1L, 2L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(matchRepository).findById(1L);
        verify(messageRepository).findByMatchId(1L, pageable);
    }

    @Test
    void testGetMessageHistoryPaginated_DifferentPageSizes() {
        Pageable pageable = PageRequest.of(1, 5);
        List<Message> messages = List.of(message);
        Page<Message> messagePage = new PageImpl<>(messages, pageable, 10);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(messageRepository.findByMatchId(1L, pageable)).thenReturn(messagePage);
        when(messageMapper.toDto(message)).thenReturn(messageDto);

        Page<MessageDto> result = messageService.getMessageHistoryPaginated(1L, 1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);
    }


    @Test
    void testDeleteMessage_Success() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        messageService.deleteMessage(1L, 1L);

        verify(messageRepository).findById(1L);
        verify(messageRepository).delete(message);
    }

    @Test
    void testDeleteMessage_MessageNotFound() {
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.deleteMessage(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Message not found");

        verify(messageRepository).findById(999L);
        verify(messageRepository, never()).delete(any(Message.class));
    }

    @Test
    void testDeleteMessage_UserNotSender() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> messageService.deleteMessage(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("access denied");

        verify(messageRepository).findById(1L);
        verify(messageRepository, never()).delete(any(Message.class));
    }

    @Test
    void testDeleteMessage_UnauthorizedUser() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> messageService.deleteMessage(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("access denied");

        verify(messageRepository).findById(1L);
        verify(messageRepository, never()).delete(any(Message.class));
    }

    @Test
    void testDeleteMessage_OnlySenderCanDelete() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> messageService.deleteMessage(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("access denied");

        verify(messageRepository, never()).delete(any(Message.class));
    }

    @Test
    void testGetMessageHistory_EmptyList() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(messageRepository.findByMatchIdOrderBySentAtAsc(1L)).thenReturn(List.of());

        List<MessageDto> result = messageService.getMessageHistory(1L, 1L);

        assertThat(result).isEmpty();
    }
}

