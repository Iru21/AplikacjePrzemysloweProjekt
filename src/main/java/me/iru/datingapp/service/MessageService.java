package me.iru.datingapp.service;

import lombok.RequiredArgsConstructor;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;

    /**
     * Sends a message within a match
     *
     * @param messageDto Message data
     * @return Saved MessageDto
     * @throws ResourceNotFoundException if sender, receiver, or match not found
     * @throws MatchNotActiveException   if the match is not active
     */
    public MessageDto sendMessage(MessageDto messageDto) {
        log.info("Sending message from user {} to user {} in match {}",
                messageDto.getSenderId(), messageDto.getReceiverId(), messageDto.getMatchId());

        User sender = userRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> {
                    log.error("Sender not found with ID: {}", messageDto.getSenderId());
                    return new ResourceNotFoundException("Sender not found with ID: " + messageDto.getSenderId());
                });

        User receiver = userRepository.findById(messageDto.getReceiverId())
                .orElseThrow(() -> {
                    log.error("Receiver not found with ID: {}", messageDto.getReceiverId());
                    return new ResourceNotFoundException("Receiver not found with ID: " + messageDto.getReceiverId());
                });

        Match match = matchRepository.findById(messageDto.getMatchId())
                .orElseThrow(() -> {
                    log.error("Match not found with ID: {}", messageDto.getMatchId());
                    return new ResourceNotFoundException("Match not found with ID: " + messageDto.getMatchId());
                });

        if (!match.getIsActive()) {
            log.error("Attempt to send message in inactive match ID: {}", messageDto.getMatchId());
            throw new MatchNotActiveException("Cannot send message: match is not active");
        }

        boolean senderInMatch = match.getUser1().getId().equals(sender.getId()) ||
                                match.getUser2().getId().equals(sender.getId());
        boolean receiverInMatch = match.getUser1().getId().equals(receiver.getId()) ||
                                  match.getUser2().getId().equals(receiver.getId());

        if (!senderInMatch || !receiverInMatch) {
            log.error("Users {} and {} are not part of match {}",
                    sender.getId(), receiver.getId(), match.getId());
            throw new ResourceNotFoundException("Users are not part of this match");
        }

        Message message = messageMapper.toEntity(messageDto, sender, receiver, match);

        Message savedMessage = messageRepository.save(message);
        log.info("Message sent successfully with ID: {}", savedMessage.getId());

        return messageMapper.toDto(savedMessage);
    }

    /**
     * Gets message history for a match, ordered by sent time
     *
     * @param matchId Match ID
     * @param userId  User ID (for authorization)
     * @return List of messages
     * @throws ResourceNotFoundException if match isn't found or user not authorized
     */
    @Transactional(readOnly = true)
    public List<MessageDto> getMessageHistory(Long matchId, Long userId) {
        log.debug("Fetching message history for match ID: {} by user ID: {}", matchId, userId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> {
                    log.error("Match not found with ID: {}", matchId);
                    return new ResourceNotFoundException("Match not found with ID: " + matchId);
                });

        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            log.error("User {} is not authorized to access messages in match {}", userId, matchId);
            throw new ResourceNotFoundException("Match not found or access denied");
        }

        List<Message> messages = messageRepository.findByMatchIdOrderBySentAtAsc(matchId);
        log.debug("Found {} messages in match ID: {}", messages.size(), matchId);

        messages.stream()
                .filter(msg -> msg.getReceiver().getId().equals(userId) && !msg.getIsRead())
                .forEach(msg -> {
                    msg.setIsRead(true);
                    messageRepository.save(msg);
                });

        return messages.stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets paginated messages for a match
     *
     * @param matchId  Match ID
     * @param userId   User ID (for authorization)
     * @param pageable Pagination parameters
     * @return Page of messages
     */
    @Transactional(readOnly = true)
    public Page<MessageDto> getMessageHistoryPaginated(Long matchId, Long userId, Pageable pageable) {
        log.debug("Fetching paginated message history for match ID: {}", matchId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + matchId));

        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            throw new ResourceNotFoundException("Match not found or access denied");
        }

        Page<Message> messages = messageRepository.findByMatchId(matchId, pageable);
        return messages.map(messageMapper::toDto);
    }

    /**
     * Deletes conversation (all messages) in a match
     *
     * @param userId  User ID
     * @param matchId Match ID
     * @throws ResourceNotFoundException if a match isn't found or user not authorized
     */
    public void deleteConversation(Long userId, Long matchId) {
        log.info("User {} deleting conversation in match ID: {}", userId, matchId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> {
                    log.error("Match not found with ID: {}", matchId);
                    return new ResourceNotFoundException("Match not found with ID: " + matchId);
                });

        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            log.error("User {} is not authorized to delete conversation in match {}", userId, matchId);
            throw new ResourceNotFoundException("Match not found or access denied");
        }

        List<Message> messages = messageRepository.findByMatchIdOrderBySentAtAsc(matchId);
        messageRepository.deleteAll(messages);

        log.info("Deleted {} messages from match ID: {}", messages.size(), matchId);
    }

    /**
     * Deletes a specific message
     *
     * @param messageId Message ID
     * @param userId    User ID (must be sender)
     * @throws ResourceNotFoundException if a message isn't found or user not authorized
     */
    public void deleteMessage(Long messageId, Long userId) {
        log.info("User {} attempting to delete message ID: {}", userId, messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    log.error("Message not found with ID: {}", messageId);
                    return new ResourceNotFoundException("Message not found with ID: " + messageId);
                });

        if (!message.getSender().getId().equals(userId)) {
            log.error("User {} is not authorized to delete message {}", userId, messageId);
            throw new ResourceNotFoundException("Message not found or access denied");
        }

        messageRepository.delete(message);
        log.info("Message ID: {} deleted by user ID: {}", messageId, userId);
    }

    /**
     * Gets unread message count for a user
     *
     * @param userId User ID
     * @return Count of unread messages
     */
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(Long userId) {
        log.debug("Fetching unread message count for user ID: {}", userId);

        List<Message> unreadMessages = messageRepository.findUnreadMessagesByUserId(userId);
        return unreadMessages.size();
    }

    /**
     * Marks a message as read
     *
     * @param messageId Message ID
     * @param userId    User ID (must be receiver)
     */
    public void markAsRead(Long messageId, Long userId) {
        log.debug("Marking message {} as read by user {}", messageId, userId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        if (!message.getReceiver().getId().equals(userId)) {
            throw new ResourceNotFoundException("Message not found or access denied");
        }

        message.setIsRead(true);
        messageRepository.save(message);
        log.debug("Message {} marked as read", messageId);
    }
}

