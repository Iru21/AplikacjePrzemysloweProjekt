package me.iru.datingapp.service;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.SearchPreference;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.mapper.UserMapper;
import me.iru.datingapp.repository.RatingRepository;
import me.iru.datingapp.repository.SearchPreferenceRepository;
import me.iru.datingapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private final UserRepository userRepository;
    private final SearchPreferenceRepository searchPreferenceRepository;
    private final RatingRepository ratingRepository;
    private final UserMapper userMapper;

    /**
     * Gets suggested users based on search preferences and excludes already rated profiles
     *
     * @param userId   Current user ID
     * @param pageable Pagination parameters
     * @return Page of suggested users
     * @throws ResourceNotFoundException if user not found
     */
    public Page<UserProfileDto> getSuggestedUsers(Long userId, Pageable pageable) {
        log.info("Fetching suggested users for user ID: {}", userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        SearchPreference preferences = searchPreferenceRepository.findByUserId(userId)
                .orElse(SearchPreference.defaultForUser(currentUser));

        log.debug("Search preferences for user {}: gender={}, age={}-{}",
                userId, preferences.getPreferredGender(), preferences.getMinAge(), preferences.getMaxAge());

        List<Long> ratedUserIds = ratingRepository.findByRaterId(userId).stream()
                .map(rating -> rating.getRatedUser().getId())
                .toList();

        log.debug("User {} has already rated {} profiles", userId, ratedUserIds.size());

        Page<User> suggestedUsers = userRepository.findBySearchCriteria(
                preferences.getPreferredGender(),
                preferences.getMinAge(),
                preferences.getMaxAge(),
                null,
                pageable
        );

        List<UserProfileDto> filteredUsers = suggestedUsers.stream()
                .filter(user -> !user.getId().equals(userId))
                .filter(user -> !ratedUserIds.contains(user.getId()))
                .map(userMapper::toDto)
                .collect(Collectors.toList());

        log.info("Found {} suggested users for user ID: {}", filteredUsers.size(), userId);

        return new PageImpl<>(filteredUsers, pageable, filteredUsers.size());
    }

    /**
     * Gets count of available suggestions for a user
     *
     * @param userId User ID
     * @return Count of available profiles
     */
    public long getAvailableSuggestionsCount(Long userId) {
        log.debug("Counting available suggestions for user ID: {}", userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        SearchPreference preferences = searchPreferenceRepository.findByUserId(userId)
                .orElse(SearchPreference.defaultForUser(currentUser));

        List<Long> ratedUserIds = ratingRepository.findByRaterId(userId).stream()
                .map(rating -> rating.getRatedUser().getId())
                .toList();

        long totalCount = userRepository.findBySearchCriteria(
                preferences.getPreferredGender(),
                preferences.getMinAge(),
                preferences.getMaxAge(),
                null,
                Pageable.unpaged()
        ).stream()
                .filter(user -> !user.getId().equals(userId))
                .filter(user -> !ratedUserIds.contains(user.getId()))
                .count();

        log.debug("User {} has {} available suggestions", userId, totalCount);
        return totalCount;
    }
}

