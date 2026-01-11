package me.iru.datingapp.service;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.MatchDto;
import me.iru.datingapp.entity.Match;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.mapper.MatchMapper;
import me.iru.datingapp.repository.MatchRepository;
import me.iru.datingapp.repository.MessageRepository;
import me.iru.datingapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MatchMapper matchMapper;

    /**
     * Gets all active matches for a user
     *
     * @param userId User ID
     * @return List of active matches
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public List<MatchDto> getActiveMatches(Long userId) {
        log.info("Fetching active matches for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        List<Match> matches = matchRepository.findActiveMatchesByUserId(userId);
        log.info("Found {} active matches for user ID: {}", matches.size(), userId);

        return matches.stream()
                .map(match -> matchMapper.toDto(match, user))
                .collect(Collectors.toList());
    }

    /**
     * Gets all matches (active and inactive) for a user
     *
     * @param userId User ID
     * @return List of all matches
     */
    @Transactional(readOnly = true)
    public List<MatchDto> getAllMatches(Long userId) {
        log.debug("Fetching all matches for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        List<Match> matches = matchRepository.findAllMatchesByUserId(userId);
        log.debug("Found {} total matches for user ID: {}", matches.size(), userId);

        return matches.stream()
                .map(match -> matchMapper.toDto(match, user))
                .collect(Collectors.toList());
    }

    /**
     * Gets a specific match by ID
     *
     * @param matchId Match ID
     * @param userId  User ID (for authorization check)
     * @return MatchDto
     * @throws ResourceNotFoundException if a match isn't found or user not authorized
     */
    @Transactional(readOnly = true)
    public MatchDto getMatchById(Long matchId, Long userId) {
        log.debug("Fetching match ID: {} for user ID: {}", matchId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> {
                    log.error("Match not found with ID: {}", matchId);
                    return new ResourceNotFoundException("Match not found with ID: " + matchId);
                });

        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            log.error("User {} is not authorized to access match {}", userId, matchId);
            throw new ResourceNotFoundException("Match not found or access denied");
        }

        return matchMapper.toDto(match, user);
    }

    /**
     * Unmatches users - deactivates match and optionally deletes message history
     *
     * @param userId  User ID initiating the unmatch
     * @param matchId Match ID
     * @throws ResourceNotFoundException if a match isn't found or user not authorized
     */
    public void unmatch(Long userId, Long matchId) {
        log.info("User {} initiating unmatch for match ID: {}", userId, matchId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> {
                    log.error("Match not found with ID: {}", matchId);
                    return new ResourceNotFoundException("Match not found with ID: " + matchId);
                });

        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            log.error("User {} is not authorized to unmatch match {}", userId, matchId);
            throw new ResourceNotFoundException("Match not found or access denied");
        }

        int deletedMessages = messageRepository.findByMatchIdOrderBySentAtAsc(matchId).size();
        messageRepository.deleteAll(messageRepository.findByMatchIdOrderBySentAtAsc(matchId));
        log.info("Deleted {} messages for match ID: {}", deletedMessages, matchId);

        match.setIsActive(false);
        matchRepository.save(match);

        log.info("Match ID: {} successfully deactivated by user ID: {}", matchId, userId);
    }

    /**
     * Permanently deletes a match and all associated messages
     *
     * @param userId  User ID
     * @param matchId Match ID
     */
    public void deleteMatch(Long userId, Long matchId) {
        log.info("User {} permanently deleting match ID: {}", userId, matchId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> {
                    log.error("Match not found with ID: {}", matchId);
                    return new ResourceNotFoundException("Match not found with ID: " + matchId);
                });

        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            log.error("User {} is not authorized to delete match {}", userId, matchId);
            throw new ResourceNotFoundException("Match not found or access denied");
        }

        matchRepository.delete(match);
        log.info("Match ID: {} permanently deleted by user ID: {}", matchId, userId);
    }

    /**
     * Checks if a match exists between two users
     *
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return true if match exists
     */
    @Transactional(readOnly = true)
    public boolean matchExists(Long userId1, Long userId2) {
        return matchRepository.existsMatchBetweenUsers(userId1, userId2);
    }

    /**
     * Gets match between two specific users
     *
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return Match if exists
     */
    @Transactional(readOnly = true)
    public Match getMatchBetweenUsers(Long userId1, Long userId2) {
        return matchRepository.findMatchBetweenUsers(userId1, userId2)
                .orElseThrow(() -> {
                    log.error("No match found between user {} and user {}", userId1, userId2);
                    return new ResourceNotFoundException(
                            "No match found between user " + userId1 + " and user " + userId2);
                });
    }
}

