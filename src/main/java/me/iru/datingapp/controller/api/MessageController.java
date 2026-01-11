package me.iru.datingapp.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Messages", description = "Messaging endpoints for matched users")
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    @Operation(summary = "Send message", description = "Send a message to a matched user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message sent successfully",
                    content = @Content(schema = @Schema(implementation = MessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid message data or match not active"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Match not found")
    })
    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@Valid @RequestBody MessageDto messageDto) {
        log.info("REST API: Send message from user {} to user {} in match {}",
                messageDto.getSenderId(), messageDto.getReceiverId(), messageDto.getMatchId());
        MessageDto savedMessage = messageService.sendMessage(messageDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMessage);
    }

    @Operation(summary = "Get message history", description = "Get all messages for a specific match")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "User not part of this match"),
            @ApiResponse(responseCode = "404", description = "Match not found")
    })
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<MessageDto>> getMessageHistory(
            @Parameter(description = "Match ID") @PathVariable Long matchId,
            @Parameter(description = "User ID") @RequestParam Long userId) {
        log.info("REST API: Get message history for match ID: {} by user ID: {}", matchId, userId);
        List<MessageDto> messages = messageService.getMessageHistory(matchId, userId);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Delete conversation", description = "Delete all messages in a conversation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conversation deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Match not found")
    })
    @DeleteMapping("/conversation/{matchId}")
    public ResponseEntity<Void> deleteConversation(
            @Parameter(description = "Match ID") @PathVariable Long matchId,
            @Parameter(description = "User ID") @RequestParam Long userId) {
        log.info("REST API: User {} deleting conversation for match ID: {}", userId, matchId);
        messageService.deleteConversation(userId, matchId);
        return ResponseEntity.noContent().build();
    }
}
