package me.iru.datingapp.mapper;

import me.iru.datingapp.dto.MessageDto;
import me.iru.datingapp.entity.Message;
import me.iru.datingapp.entity.Match;
import me.iru.datingapp.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageMapper {


    public MessageDto toDto(Message message) {
        if (message == null) {
            return null;
        }

        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setMatchId(message.getMatch().getId());
        dto.setContent(message.getContent());
        dto.setSentAt(message.getSentAt());
        dto.setIsRead(message.getIsRead());

        dto.setSenderName(message.getSender().getFirstName() + " " + message.getSender().getLastName());
        dto.setReceiverName(message.getReceiver().getFirstName() + " " + message.getReceiver().getLastName());

        return dto;
    }

    public Message toEntity(MessageDto dto, User sender, User receiver, Match match) {
        if (dto == null) {
            return null;
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setMatch(match);
        message.setContent(dto.getContent());
        message.setIsRead(false);

        return message;
    }


    public List<MessageDto> toDtoList(List<Message> messages) {
        if (messages == null) {
            return Collections.emptyList();
        }

        return messages.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    public void markAsRead(Message message) {
        if (message != null) {
            message.setIsRead(true);
        }
    }
}

