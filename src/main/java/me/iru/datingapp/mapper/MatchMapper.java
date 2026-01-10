package me.iru.datingapp.mapper;

import me.iru.datingapp.dto.MatchDto;
import me.iru.datingapp.entity.Match;
import me.iru.datingapp.entity.Message;
import me.iru.datingapp.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MatchMapper {

    private final MessageMapper messageMapper;

    public MatchMapper(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }


    public MatchDto toDto(Match match, User currentUser) {
        if (match == null || currentUser == null) {
            return null;
        }

        User otherUser = match.getOtherUser(currentUser);
        if (otherUser == null) {
            return null;
        }

        MatchDto dto = new MatchDto();
        dto.setId(match.getId());
        dto.setMatchedUserId(otherUser.getId());
        dto.setMatchedUserName(otherUser.getFirstName() + " " + otherUser.getLastName());
        dto.setMatchedUserPhotoUrl(otherUser.getPhotoUrl());
        dto.setMatchedUserAge(otherUser.getAge());
        dto.setMatchedUserCity(otherUser.getCity());
        dto.setMatchedAt(match.getMatchedAt());
        dto.setIsActive(match.getIsActive());

        if (match.getMessages() != null && !match.getMessages().isEmpty()) {
            match.getMessages().stream()
                    .max(Comparator.comparing(Message::getSentAt))
                    .ifPresent(lastMessage -> dto.setLastMessage(messageMapper.toDto(lastMessage)));
        }

        return dto;
    }

    public MatchDto toDto(Match match) {
        if (match == null) {
            return null;
        }

        return toDto(match, match.getUser1());
    }


    public List<MatchDto> toDtoList(List<Match> matches, User currentUser) {
        if (matches == null) {
            return Collections.emptyList();
        }

        return matches.stream()
                .map(match -> toDto(match, currentUser))
                .collect(Collectors.toList());
    }


    public Match createMatch(User user1, User user2) {
        if (user1 == null || user2 == null) {
            return null;
        }

        Match match = new Match();
        match.setUser1(user1);
        match.setUser2(user2);
        match.setIsActive(true);

        return match;
    }
}

