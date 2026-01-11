package me.iru.datingapp.mapper;

import me.iru.datingapp.dto.MessageDto;
import me.iru.datingapp.entity.Match;
import me.iru.datingapp.entity.Message;
import me.iru.datingapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MessageMapperTest {

    private MessageMapper messageMapper;
    private User sender;
    private User receiver;
    private Match match;
    private Message testMessage;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        messageMapper = new MessageMapper();
        testDateTime = LocalDateTime.of(2026, 1, 12, 10, 30);

        sender = new User();
        sender.setId(1L);
        sender.setEmail("sender@example.com");
        sender.setFirstName("John");
        sender.setLastName("Doe");

        receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");
        receiver.setFirstName("Jane");
        receiver.setLastName("Smith");

        match = new Match();
        match.setId(1L);
        match.setUser1(sender);
        match.setUser2(receiver);
        match.setIsActive(true);

        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setSender(sender);
        testMessage.setReceiver(receiver);
        testMessage.setMatch(match);
        testMessage.setContent("Hello, how are you?");
        testMessage.setSentAt(testDateTime);
        testMessage.setIsRead(false);
    }


    @Test
    void testToDto_Success() {
        MessageDto result = messageMapper.toDto(testMessage);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSenderId()).isEqualTo(1L);
        assertThat(result.getReceiverId()).isEqualTo(2L);
        assertThat(result.getMatchId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("Hello, how are you?");
        assertThat(result.getSentAt()).isEqualTo(testDateTime);
        assertThat(result.getIsRead()).isFalse();
        assertThat(result.getSenderName()).isEqualTo("John Doe");
        assertThat(result.getReceiverName()).isEqualTo("Jane Smith");
    }

    @Test
    void testToDto_WithNullMessage() {
        MessageDto result = messageMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    void testToDto_MessageIsRead() {
        testMessage.setIsRead(true);

        MessageDto result = messageMapper.toDto(testMessage);

        assertThat(result).isNotNull();
        assertThat(result.getIsRead()).isTrue();
    }

    @Test
    void testToDto_LongMessage() {
        String longContent = "This is a very long message with lots of text. ".repeat(10);
        testMessage.setContent(longContent);

        MessageDto result = messageMapper.toDto(testMessage);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(longContent);
    }

    @Test
    void testToDto_SenderAndReceiverNamesFormatted() {
        sender.setFirstName("Alexander");
        sender.setLastName("von Humboldt");
        receiver.setFirstName("Marie");
        receiver.setLastName("Curie-Sklodowska");

        MessageDto result = messageMapper.toDto(testMessage);

        assertThat(result.getSenderName()).isEqualTo("Alexander von Humboldt");
        assertThat(result.getReceiverName()).isEqualTo("Marie Curie-Sklodowska");
    }


    @Test
    void testToEntity_Success() {
        MessageDto dto = new MessageDto();
        dto.setContent("Test message content");

        Message result = messageMapper.toEntity(dto, sender, receiver, match);

        assertThat(result).isNotNull();
        assertThat(result.getSender()).isEqualTo(sender);
        assertThat(result.getReceiver()).isEqualTo(receiver);
        assertThat(result.getMatch()).isEqualTo(match);
        assertThat(result.getContent()).isEqualTo("Test message content");
        assertThat(result.getIsRead()).isFalse();
        assertThat(result.getId()).isNull();
        assertThat(result.getSentAt()).isNull();
    }

    @Test
    void testToEntity_WithNullDto() {
        Message result = messageMapper.toEntity(null, sender, receiver, match);

        assertThat(result).isNull();
    }

    @Test
    void testToEntity_AlwaysSetIsReadToFalse() {
        MessageDto dto = new MessageDto();
        dto.setContent("New message");
        dto.setIsRead(true);
        Message result = messageMapper.toEntity(dto, sender, receiver, match);

        assertThat(result.getIsRead()).isFalse();
    }

    @Test
    void testToEntity_EmptyContent() {
        MessageDto dto = new MessageDto();
        dto.setContent("");

        Message result = messageMapper.toEntity(dto, sender, receiver, match);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void testToEntity_WithNullContent() {
        MessageDto dto = new MessageDto();
        dto.setContent(null);

        Message result = messageMapper.toEntity(dto, sender, receiver, match);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNull();
    }


    @Test
    void testToDtoList_Success() {
        Message message2 = new Message();
        message2.setId(2L);
        message2.setSender(receiver);
        message2.setReceiver(sender);
        message2.setMatch(match);
        message2.setContent("Reply message");
        message2.setSentAt(testDateTime.plusMinutes(5));
        message2.setIsRead(true);

        List<Message> messages = List.of(testMessage, message2);

        List<MessageDto> result = messageMapper.toDtoList(messages);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("Hello, how are you?");
        assertThat(result.get(1).getContent()).isEqualTo("Reply message");
    }

    @Test
    void testToDtoList_EmptyList() {
        List<MessageDto> result = messageMapper.toDtoList(List.of());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void testToDtoList_WithNullList() {
        List<MessageDto> result = messageMapper.toDtoList(null);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void testToDtoList_SingleMessage() {
        List<MessageDto> result = messageMapper.toDtoList(List.of(testMessage));

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void testToDtoList_PreservesOrder() {
        Message message2 = new Message();
        message2.setId(2L);
        message2.setSender(sender);
        message2.setReceiver(receiver);
        message2.setMatch(match);
        message2.setContent("Message 2");
        message2.setSentAt(testDateTime.plusMinutes(1));
        message2.setIsRead(false);

        Message message3 = new Message();
        message3.setId(3L);
        message3.setSender(sender);
        message3.setReceiver(receiver);
        message3.setMatch(match);
        message3.setContent("Message 3");
        message3.setSentAt(testDateTime.plusMinutes(2));
        message3.setIsRead(false);

        List<Message> messages = List.of(testMessage, message2, message3);

        List<MessageDto> result = messageMapper.toDtoList(messages);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(2).getId()).isEqualTo(3L);
    }


    @Test
    void testMarkAsRead_Success() {
        assertThat(testMessage.getIsRead()).isFalse();

        messageMapper.markAsRead(testMessage);

        assertThat(testMessage.getIsRead()).isTrue();
    }

    @Test
    void testMarkAsRead_AlreadyRead() {
        testMessage.setIsRead(true);

        messageMapper.markAsRead(testMessage);

        assertThat(testMessage.getIsRead()).isTrue();
    }

    @Test
    void testMarkAsRead_WithNullMessage() {
        assertThatCode(() -> messageMapper.markAsRead(null))
                .doesNotThrowAnyException();
    }

    @Test
    void testMarkAsRead_DoesNotModifyOtherFields() {
        String originalContent = testMessage.getContent();
        LocalDateTime originalSentAt = testMessage.getSentAt();
        User originalSender = testMessage.getSender();
        User originalReceiver = testMessage.getReceiver();

        messageMapper.markAsRead(testMessage);

        assertThat(testMessage.getContent()).isEqualTo(originalContent);
        assertThat(testMessage.getSentAt()).isEqualTo(originalSentAt);
        assertThat(testMessage.getSender()).isEqualTo(originalSender);
        assertThat(testMessage.getReceiver()).isEqualTo(originalReceiver);
    }


    @Test
    void testToDto_WithSpecialCharacters() {
        testMessage.setContent("Hello! 😊 How are you? #test @user");

        MessageDto result = messageMapper.toDto(testMessage);

        assertThat(result.getContent()).isEqualTo("Hello! 😊 How are you? #test @user");
    }

    @Test
    void testToDto_WithNewlines() {
        testMessage.setContent("Line 1\nLine 2\nLine 3");

        MessageDto result = messageMapper.toDto(testMessage);

        assertThat(result.getContent()).isEqualTo("Line 1\nLine 2\nLine 3");
    }

    @Test
    void testToEntity_WithSpecialCharacters() {
        MessageDto dto = new MessageDto();
        dto.setContent("Special chars: @#$%^&*()");

        Message result = messageMapper.toEntity(dto, sender, receiver, match);

        assertThat(result.getContent()).isEqualTo("Special chars: @#$%^&*()");
    }

    @Test
    void testToDtoList_MixedReadStatus() {
        Message readMessage = new Message();
        readMessage.setId(2L);
        readMessage.setSender(sender);
        readMessage.setReceiver(receiver);
        readMessage.setMatch(match);
        readMessage.setContent("Read message");
        readMessage.setSentAt(testDateTime);
        readMessage.setIsRead(true);

        List<Message> messages = List.of(testMessage, readMessage);

        List<MessageDto> result = messageMapper.toDtoList(messages);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIsRead()).isFalse();
        assertThat(result.get(1).getIsRead()).isTrue();
    }
}

