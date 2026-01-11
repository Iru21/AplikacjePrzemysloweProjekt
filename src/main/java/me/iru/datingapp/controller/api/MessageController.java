package me.iru.datingapp.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.MessageDto;
import me.iru.datingapp.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@Valid @RequestBody MessageDto messageDto) {
        log.info("REST API: Send message from user {} to user {} in match {}",
                messageDto.getSenderId(), messageDto.getReceiverId(), messageDto.getMatchId());
        MessageDto savedMessage = messageService.sendMessage(messageDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMessage);
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<MessageDto>> getMessageHistory(
            @PathVariable Long matchId,
            @RequestParam Long userId) {
        log.info("REST API: Get message history for match ID: {} by user ID: {}", matchId, userId);
        List<MessageDto> messages = messageService.getMessageHistory(matchId, userId);
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/conversation/{matchId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long matchId,
            @RequestParam Long userId) {
        log.info("REST API: User {} deleting conversation for match ID: {}", userId, matchId);
        messageService.deleteConversation(userId, matchId);
        return ResponseEntity.noContent().build();
    }
}

