package me.iru.datingapp.service;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.entity.Interest;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.entity.UserInterest;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.repository.InterestRepository;
import me.iru.datingapp.repository.UserInterestRepository;
import me.iru.datingapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InterestService {

    private static final Logger log = LoggerFactory.getLogger(InterestService.class);

    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserRepository userRepository;

    /**
     * Gets all available interests, sorted by name
     *
     * @return List of all interests
     */
    @Transactional(readOnly = true)
    public List<Interest> getAllInterests() {
        log.debug("Fetching all interests");
        List<Interest> interests = interestRepository.findAllByOrderByNameAsc();
        log.debug("Found {} interests", interests.size());
        return interests;
    }

    /**
     * Gets a specific interest by ID
     *
     * @param id Interest ID
     * @return Interest
     * @throws ResourceNotFoundException if interest not found
     */
    @Transactional(readOnly = true)
    public Interest getInterestById(Long id) {
        log.debug("Fetching interest with ID: {}", id);
        return interestRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Interest not found with ID: {}", id);
                    return new ResourceNotFoundException("Interest not found with ID: " + id);
                });
    }

    /**
     * Gets a specific interest by name
     *
     * @param name Interest name
     * @return Interest
     * @throws ResourceNotFoundException if interest not found
     */
    @Transactional(readOnly = true)
    public Interest getInterestByName(String name) {
        log.debug("Fetching interest with name: {}", name);
        return interestRepository.findByName(name)
                .orElseThrow(() -> {
                    log.error("Interest not found with name: {}", name);
                    return new ResourceNotFoundException("Interest not found with name: " + name);
                });
    }

    /**
     * Adds an interest to a user's profile
     *
     * @param userId     User ID
     * @param interestId Interest ID
     * @throws ResourceNotFoundException    if user or interest not found
     * @throws IllegalArgumentException     if the user already has this interest
     */
    public void addInterestToUser(Long userId, Long interestId) {
        log.info("Adding interest {} to user {}", interestId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> {
                    log.error("Interest not found with ID: {}", interestId);
                    return new ResourceNotFoundException("Interest not found with ID: " + interestId);
                });

        if (userInterestRepository.existsByUserIdAndInterestId(userId, interestId)) {
            log.warn("User {} already has interest {}", userId, interestId);
            throw new IllegalArgumentException("User already has this interest");
        }

        UserInterest userInterest = new UserInterest();
        userInterest.setUser(user);
        userInterest.setInterest(interest);
        userInterestRepository.save(userInterest);

        log.info("Successfully added interest {} to user {}", interestId, userId);
    }

    /**
     * Removes an interest from a user's profile
     *
     * @param userId     User ID
     * @param interestId Interest ID
     * @throws ResourceNotFoundException if user or interest not found
     */
    public void removeInterestFromUser(Long userId, Long interestId) {
        log.info("Removing interest {} from user {}", interestId, userId);

        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        if (!interestRepository.existsById(interestId)) {
            log.error("Interest not found with ID: {}", interestId);
            throw new ResourceNotFoundException("Interest not found with ID: " + interestId);
        }

        if (!userInterestRepository.existsByUserIdAndInterestId(userId, interestId)) {
            log.warn("User {} does not have interest {}", userId, interestId);
            return;
        }

        userInterestRepository.deleteByUserIdAndInterestId(userId, interestId);
        log.info("Successfully removed interest {} from user {}", interestId, userId);
    }

    /**
     * Gets all interests for a specific user
     *
     * @param userId User ID
     * @return List of user's interests
     */
    @Transactional(readOnly = true)
    public List<Interest> getUserInterests(Long userId) {
        log.debug("Fetching interests for user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        List<UserInterest> userInterests = userInterestRepository.findByUserId(userId);
        List<Interest> interests = userInterests.stream()
                .map(UserInterest::getInterest)
                .toList();

        log.debug("User {} has {} interests", userId, interests.size());
        return interests;
    }

    /**
     * Creates a new interest (admin function)
     *
     * @param interest Interest to create
     * @return Created interest
     */
    public Interest createInterest(Interest interest) {
        log.info("Creating new interest: {}", interest.getName());

        if (interestRepository.existsByName(interest.getName())) {
            log.error("Interest already exists with name: {}", interest.getName());
            throw new IllegalArgumentException("Interest already exists with name: " + interest.getName());
        }

        Interest savedInterest = interestRepository.save(interest);
        log.info("Successfully created interest with ID: {}", savedInterest.getId());
        return savedInterest;
    }

    /**
     * Updates an existing interest (admin function)
     *
     * @param id       Interest ID
     * @param interest Updated interest data
     * @return Updated interest
     */
    public Interest updateInterest(Long id, Interest interest) {
        log.info("Updating interest with ID: {}", id);

        Interest existingInterest = interestRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Interest not found with ID: {}", id);
                    return new ResourceNotFoundException("Interest not found with ID: " + id);
                });

        existingInterest.setName(interest.getName());
        existingInterest.setDescription(interest.getDescription());

        Interest updatedInterest = interestRepository.save(existingInterest);
        log.info("Successfully updated interest with ID: {}", id);
        return updatedInterest;
    }

    /**
     * Deletes an interest (admin function)
     *
     * @param id Interest ID
     */
    public void deleteInterest(Long id) {
        log.info("Deleting interest with ID: {}", id);

        Interest interest = interestRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Interest not found with ID: {}", id);
                    return new ResourceNotFoundException("Interest not found with ID: " + id);
                });

        interestRepository.delete(interest);
        log.info("Successfully deleted interest with ID: {}", id);
    }
}

