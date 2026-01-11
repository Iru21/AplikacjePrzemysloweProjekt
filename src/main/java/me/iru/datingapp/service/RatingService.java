package me.iru.datingapp.service;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.RatingDto;
import me.iru.datingapp.entity.Match;
import me.iru.datingapp.entity.Rating;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.repository.MatchRepository;
import me.iru.datingapp.repository.RatingRepository;
import me.iru.datingapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingService.class);

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    /**
     * Rates a user (LIKE or DISLIKE) and creates a match if mutual LIKE exists
     *
     * @param raterId    ID of user giving the rating
     * @param ratingDto  Rating data (rated user ID and rating type)
     * @throws ResourceNotFoundException if user not found
     */
    public void rateUser(Long raterId, RatingDto ratingDto) {
        log.info("User {} rating user {} with {}", raterId, ratingDto.getRatedUserId(), ratingDto.getRatingType());

        User rater = userRepository.findById(raterId)
                .orElseThrow(() -> {
                    log.error("Rater not found with ID: {}", raterId);
                    return new ResourceNotFoundException("User not found with ID: " + raterId);
                });

        User ratedUser = userRepository.findById(ratingDto.getRatedUserId())
                .orElseThrow(() -> {
                    log.error("Rated user not found with ID: {}", ratingDto.getRatedUserId());
                    return new ResourceNotFoundException("User not found with ID: " + ratingDto.getRatedUserId());
                });

        if (ratingRepository.existsByRaterIdAndRatedUserId(raterId, ratingDto.getRatedUserId())) {
            log.warn("User {} has already rated user {}", raterId, ratingDto.getRatedUserId());
            return;
        }

        Rating rating = new Rating();
        rating.setRater(rater);
        rating.setRatedUser(ratedUser);
        rating.setRatingType(ratingDto.getRatingType());
        ratingRepository.save(rating);

        log.info("Rating saved: {} rated {} as {}", raterId, ratingDto.getRatedUserId(), ratingDto.getRatingType());

        if (ratingDto.getRatingType() == Rating.RatingType.LIKE) {
            checkAndCreateMatch(rater, ratedUser);
        }
    }

    /**
     * Checks if there's a mutual LIKE and creates a match
     *
     * @param user1 First user
     * @param user2 Second user
     */
    private void checkAndCreateMatch(User user1, User user2) {
        log.debug("Checking for mutual like between user {} and user {}", user1.getId(), user2.getId());

        Optional<Rating> reciprocalRating = ratingRepository.findByRaterIdAndRatedUserId(
                user2.getId(), user1.getId()
        );

        if (reciprocalRating.isPresent() &&
            reciprocalRating.get().getRatingType() == Rating.RatingType.LIKE) {

            if (!matchRepository.existsMatchBetweenUsers(user1.getId(), user2.getId())) {

                Match match = new Match();
                match.setUser1(user1);
                match.setUser2(user2);
                match.setIsActive(true);
                matchRepository.save(match);

                log.info("User {} and User {} matched!", user1.getId(), user2.getId());

                // TODO: Send notification to both users
            } else {
                log.debug("Match already exists between user {} and user {}", user1.getId(), user2.getId());
            }
        } else {
            log.debug("No mutual like yet between user {} and user {}", user1.getId(), user2.getId());
        }
    }

    /**
     * Gets rating given by one user to another
     *
     * @param raterId      ID of the user who gave rating
     * @param ratedUserId  ID of user who received rating
     * @return Rating if exists
     */
    @Transactional(readOnly = true)
    public Optional<Rating> getRating(Long raterId, Long ratedUserId) {
        log.debug("Fetching rating from user {} to user {}", raterId, ratedUserId);
        return ratingRepository.findByRaterIdAndRatedUserId(raterId, ratedUserId);
    }

    /**
     * Checks if a mutual like exists between two users
     *
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return true if mutual like exists
     */
    @Transactional(readOnly = true)
    public boolean hasMutualLike(Long userId1, Long userId2) {
        log.debug("Checking mutual like between user {} and user {}", userId1, userId2);
        return ratingRepository.existsMutualLike(userId1, userId2);
    }

    /**
     * Deletes a rating
     *
     * @param raterId      ID of the user who gave rating
     * @param ratedUserId  ID of user who received rating
     */
    public void deleteRating(Long raterId, Long ratedUserId) {
        log.info("Deleting rating from user {} to user {}", raterId, ratedUserId);

        Optional<Rating> rating = ratingRepository.findByRaterIdAndRatedUserId(raterId, ratedUserId);
        rating.ifPresent(r -> {
            ratingRepository.delete(r);
            log.info("Rating deleted successfully");
        });
    }
}

