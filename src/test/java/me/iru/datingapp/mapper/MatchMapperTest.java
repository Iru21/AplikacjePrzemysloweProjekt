package me.iru.datingapp.mapper;

import me.iru.datingapp.dto.MatchDto;
import me.iru.datingapp.dto.MessageDto;
import me.iru.datingapp.entity.Match;
import me.iru.datingapp.entity.Message;
import me.iru.datingapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchMapperTest {

    @Mock
    private MessageMapper messageMapper;

    private MatchMapper matchMapper;
    private User user1;
    private User user2;
    private Match testMatch;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        matchMapper = new MatchMapper(messageMapper);
        testDateTime = LocalDateTime.of(2026, 1, 12, 10, 30);

        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setAge(25);
        user1.setCity("Warsaw");
        user1.setPhotoUrl("/photos/user1.jpg");

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setAge(28);
        user2.setCity("Krakow");
        user2.setPhotoUrl("/photos/user2.jpg");

        testMatch = new Match();
        testMatch.setId(1L);
        testMatch.setUser1(user1);
        testMatch.setUser2(user2);
        testMatch.setMatchedAt(testDateTime);
        testMatch.setIsActive(true);
        testMatch.setMessages(new ArrayList<>());
    }


    @Test
    void testToDto_WithCurrentUser_User1Perspective() {
        MatchDto result = matchMapper.toDto(testMatch, user1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMatchedUserId()).isEqualTo(2L);
        assertThat(result.getMatchedUserName()).isEqualTo("Jane Smith");
        assertThat(result.getMatchedUserPhotoUrl()).isEqualTo("/photos/user2.jpg");
        assertThat(result.getMatchedUserAge()).isEqualTo(28);
        assertThat(result.getMatchedUserCity()).isEqualTo("Krakow");
        assertThat(result.getMatchedAt()).isEqualTo(testDateTime);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getLastMessage()).isNull();
    }

    @Test
    void testToDto_WithCurrentUser_User2Perspective() {
        MatchDto result = matchMapper.toDto(testMatch, user2);

        assertThat(result).isNotNull();
        assertThat(result.getMatchedUserId()).isEqualTo(1L);
        assertThat(result.getMatchedUserName()).isEqualTo("John Doe");
        assertThat(result.getMatchedUserPhotoUrl()).isEqualTo("/photos/user1.jpg");
        assertThat(result.getMatchedUserAge()).isEqualTo(25);
        assertThat(result.getMatchedUserCity()).isEqualTo("Warsaw");
    }

    @Test
    void testToDto_WithNullMatch() {
        MatchDto result = matchMapper.toDto(null, user1);

        assertThat(result).isNull();
    }

    @Test
    void testToDto_WithNullCurrentUser() {
        MatchDto result = matchMapper.toDto(testMatch, null);

        assertThat(result).isNull();
    }

    @Test
    void testToDto_WithInactiveMatch() {
        testMatch.setIsActive(false);

        MatchDto result = matchMapper.toDto(testMatch, user1);

        assertThat(result).isNotNull();
        assertThat(result.getIsActive()).isFalse();
    }

    @Test
    void testToDto_WithMessages() {
        Message message1 = new Message();
        message1.setId(1L);
        message1.setSentAt(testDateTime);
        message1.setContent("Message 1");

        Message message2 = new Message();
        message2.setId(2L);
        message2.setSentAt(testDateTime.plusMinutes(5));
        message2.setContent("Message 2");

        Message message3 = new Message();
        message3.setId(3L);
        message3.setSentAt(testDateTime.plusMinutes(10));
        message3.setContent("Message 3 - Latest");

        testMatch.setMessages(List.of(message1, message2, message3));

        MessageDto lastMessageDto = new MessageDto();
        lastMessageDto.setId(3L);
        lastMessageDto.setContent("Message 3 - Latest");

        when(messageMapper.toDto(message3)).thenReturn(lastMessageDto);

        MatchDto result = matchMapper.toDto(testMatch, user1);

        assertThat(result).isNotNull();
        assertThat(result.getLastMessage()).isNotNull();
        assertThat(result.getLastMessage().getId()).isEqualTo(3L);
        assertThat(result.getLastMessage().getContent()).isEqualTo("Message 3 - Latest");
        verify(messageMapper).toDto(message3);
    }

    @Test
    void testToDto_WithEmptyMessages() {
        testMatch.setMessages(new ArrayList<>());

        MatchDto result = matchMapper.toDto(testMatch, user1);

        assertThat(result).isNotNull();
        assertThat(result.getLastMessage()).isNull();
        verifyNoInteractions(messageMapper);
    }

    @Test
    void testToDto_WithNullMessages() {
        testMatch.setMessages(null);

        MatchDto result = matchMapper.toDto(testMatch, user1);

        assertThat(result).isNotNull();
        assertThat(result.getLastMessage()).isNull();
        verifyNoInteractions(messageMapper);
    }

    @Test
    void testToDto_WithSingleMessage() {
        Message message = new Message();
        message.setId(1L);
        message.setSentAt(testDateTime);
        message.setContent("Only message");

        testMatch.setMessages(List.of(message));

        MessageDto messageDto = new MessageDto();
        messageDto.setId(1L);
        messageDto.setContent("Only message");

        when(messageMapper.toDto(message)).thenReturn(messageDto);

        MatchDto result = matchMapper.toDto(testMatch, user1);

        assertThat(result.getLastMessage()).isNotNull();
        assertThat(result.getLastMessage().getId()).isEqualTo(1L);
    }

    @Test
    void testToDto_UserNotInMatch() {
        User user3 = new User();
        user3.setId(3L);

        MatchDto result = matchMapper.toDto(testMatch, user3);

        assertThat(result).isNull();
    }


    @Test
    void testToDto_WithoutCurrentUser() {
        MatchDto result = matchMapper.toDto(testMatch);

        assertThat(result).isNotNull();
        assertThat(result.getMatchedUserId()).isEqualTo(2L);
        assertThat(result.getMatchedUserName()).isEqualTo("Jane Smith");
    }

    @Test
    void testToDto_WithoutCurrentUser_NullMatch() {
        MatchDto result = matchMapper.toDto(null);

        assertThat(result).isNull();
    }


    @Test
    void testToDtoList_Success() {
        Match match2 = new Match();
        match2.setId(2L);
        match2.setUser1(user1);
        match2.setUser2(user2);
        match2.setMatchedAt(testDateTime.plusDays(1));
        match2.setIsActive(true);
        match2.setMessages(new ArrayList<>());

        List<Match> matches = List.of(testMatch, match2);

        List<MatchDto> result = matchMapper.toDtoList(matches, user1);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void testToDtoList_EmptyList() {
        List<MatchDto> result = matchMapper.toDtoList(List.of(), user1);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void testToDtoList_WithNullList() {
        List<MatchDto> result = matchMapper.toDtoList(null, user1);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void testToDtoList_SingleMatch() {
        List<MatchDto> result = matchMapper.toDtoList(List.of(testMatch), user1);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMatchedUserId()).isEqualTo(2L);
    }

    @Test
    void testToDtoList_PreservesOrder() {
        Match match2 = new Match();
        match2.setId(2L);
        match2.setUser1(user1);
        match2.setUser2(user2);
        match2.setMatchedAt(testDateTime);
        match2.setIsActive(true);
        match2.setMessages(new ArrayList<>());

        Match match3 = new Match();
        match3.setId(3L);
        match3.setUser1(user1);
        match3.setUser2(user2);
        match3.setMatchedAt(testDateTime);
        match3.setIsActive(true);
        match3.setMessages(new ArrayList<>());

        List<Match> matches = List.of(testMatch, match2, match3);

        List<MatchDto> result = matchMapper.toDtoList(matches, user1);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(2).getId()).isEqualTo(3L);
    }

    @Test
    void testToDtoList_DifferentPerspectives() {
        User user3 = new User();
        user3.setId(3L);
        user3.setFirstName("Bob");
        user3.setLastName("Johnson");
        user3.setAge(30);
        user3.setCity("Gdansk");

        Match match2 = new Match();
        match2.setId(2L);
        match2.setUser1(user1);
        match2.setUser2(user3);
        match2.setMatchedAt(testDateTime);
        match2.setIsActive(true);
        match2.setMessages(new ArrayList<>());

        List<Match> matches = List.of(testMatch, match2);

        List<MatchDto> result = matchMapper.toDtoList(matches, user1);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMatchedUserName()).isEqualTo("Jane Smith");
        assertThat(result.get(1).getMatchedUserName()).isEqualTo("Bob Johnson");
    }


    @Test
    void testCreateMatch_Success() {
        Match result = matchMapper.createMatch(user1, user2);

        assertThat(result).isNotNull();
        assertThat(result.getUser1()).isEqualTo(user1);
        assertThat(result.getUser2()).isEqualTo(user2);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getId()).isNull();
        assertThat(result.getMatchedAt()).isNull();
    }

    @Test
    void testCreateMatch_WithNullUser1() {
        Match result = matchMapper.createMatch(null, user2);

        assertThat(result).isNull();
    }

    @Test
    void testCreateMatch_WithNullUser2() {
        Match result = matchMapper.createMatch(user1, null);

        assertThat(result).isNull();
    }

    @Test
    void testCreateMatch_WithBothUsersNull() {
        Match result = matchMapper.createMatch(null, null);

        assertThat(result).isNull();
    }

    @Test
    void testCreateMatch_AlwaysActiveByDefault() {
        Match result = matchMapper.createMatch(user1, user2);

        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void testCreateMatch_WithSameUsers() {
        Match result = matchMapper.createMatch(user1, user1);

        assertThat(result).isNotNull();
        assertThat(result.getUser1()).isEqualTo(user1);
        assertThat(result.getUser2()).isEqualTo(user1);
    }


    @Test
    void testToDto_WithNullPhotoUrl() {
        user2.setPhotoUrl(null);

        MatchDto result = matchMapper.toDto(testMatch, user1);

        assertThat(result).isNotNull();
        assertThat(result.getMatchedUserPhotoUrl()).isNull();
    }

    @Test
    void testToDto_WithNullCity() {
        user2.setCity(null);

        MatchDto result = matchMapper.toDto(testMatch, user1);

        assertThat(result).isNotNull();
        assertThat(result.getMatchedUserCity()).isNull();
    }

    @Test
    void testToDto_WithLongNames() {
        user2.setFirstName("Alexander");
        user2.setLastName("von Humboldt-Wellington");

        MatchDto result = matchMapper.toDto(testMatch, user1);

        assertThat(result.getMatchedUserName()).isEqualTo("Alexander von Humboldt-Wellington");
    }

    @Test
    void testToDto_MessagesInRandomOrder() {
        Message oldest = new Message();
        oldest.setId(1L);
        oldest.setSentAt(testDateTime);

        Message newest = new Message();
        newest.setId(3L);
        newest.setSentAt(testDateTime.plusMinutes(10));

        Message middle = new Message();
        middle.setId(2L);
        middle.setSentAt(testDateTime.plusMinutes(5));

        testMatch.setMessages(List.of(middle, newest, oldest));

        MessageDto newestDto = new MessageDto();
        newestDto.setId(3L);
        when(messageMapper.toDto(newest)).thenReturn(newestDto);

        MatchDto result = matchMapper.toDto(testMatch, user1);

        assertThat(result.getLastMessage()).isNotNull();
        assertThat(result.getLastMessage().getId()).isEqualTo(3L);
    }

    @Test
    void testCreateMatch_OrderDoesNotMatter() {
        Match result1 = matchMapper.createMatch(user1, user2);
        Match result2 = matchMapper.createMatch(user2, user1);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getUser1()).isEqualTo(user1);
        assertThat(result2.getUser1()).isEqualTo(user2);
    }

    @Test
    void testToDtoList_MixedActiveStatus() {
        Match inactiveMatch = new Match();
        inactiveMatch.setId(2L);
        inactiveMatch.setUser1(user1);
        inactiveMatch.setUser2(user2);
        inactiveMatch.setMatchedAt(testDateTime);
        inactiveMatch.setIsActive(false);
        inactiveMatch.setMessages(new ArrayList<>());

        List<Match> matches = List.of(testMatch, inactiveMatch);

        List<MatchDto> result = matchMapper.toDtoList(matches, user1);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIsActive()).isTrue();
        assertThat(result.get(1).getIsActive()).isFalse();
    }
}

