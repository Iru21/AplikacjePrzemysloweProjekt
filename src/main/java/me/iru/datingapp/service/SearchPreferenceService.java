package me.iru.datingapp.service;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.SearchPreferenceDto;
import me.iru.datingapp.entity.SearchPreference;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.mapper.SearchPreferenceMapper;
import me.iru.datingapp.repository.SearchPreferenceRepository;
import me.iru.datingapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchPreferenceService {

    private static final Logger log = LoggerFactory.getLogger(SearchPreferenceService.class);

    private final SearchPreferenceRepository searchPreferenceRepository;
    private final UserRepository userRepository;
    private final SearchPreferenceMapper searchPreferenceMapper;

    /**
     * Gets search preferences for a user
     *
     * @param userId User ID
     * @return SearchPreferenceDto
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public SearchPreferenceDto getPreferences(Long userId) {
        log.debug("Fetching search preferences for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        SearchPreference preference = searchPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(user));

        return searchPreferenceMapper.toDto(preference);
    }

    /**
     * Updates or creates search preferences for a user
     *
     * @param userId User ID
     * @param dto    Search preference data
     * @return Updated SearchPreferenceDto
     * @throws ResourceNotFoundException if user not found
     */
    public SearchPreferenceDto updatePreferences(Long userId, SearchPreferenceDto dto) {
        log.info("Updating search preferences for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        SearchPreference preference = searchPreferenceRepository.findByUserId(userId)
                .orElse(new SearchPreference());

        // Update preference fields
        preference.setUser(user);
        searchPreferenceMapper.updateEntityFromDto(dto, preference);

        // Validate age range
        if (preference.getMinAge() != null && preference.getMaxAge() != null) {
            if (preference.getMinAge() > preference.getMaxAge()) {
                log.error("Invalid age range: min age {} is greater than max age {}",
                        preference.getMinAge(), preference.getMaxAge());
                throw new IllegalArgumentException("Min age cannot be greater than max age");
            }
            if (preference.getMinAge() < 18) {
                log.error("Min age {} is below 18", preference.getMinAge());
                throw new IllegalArgumentException("Min age must be at least 18");
            }
        }

        SearchPreference savedPreference = searchPreferenceRepository.save(preference);
        log.info("Successfully updated search preferences for user ID: {}", userId);

        return searchPreferenceMapper.toDto(savedPreference);
    }

    /**
     * Deletes search preferences for a user
     *
     * @param userId User ID
     */
    public void deletePreferences(Long userId) {
        log.info("Deleting search preferences for user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        searchPreferenceRepository.deleteByUserId(userId);
        log.info("Successfully deleted search preferences for user ID: {}", userId);
    }

    /**
     * Checks if user has custom search preferences set
     *
     * @param userId User ID
     * @return true if preferences exist
     */
    @Transactional(readOnly = true)
    public boolean hasPreferences(Long userId) {
        return searchPreferenceRepository.existsByUserId(userId);
    }

    /**
     * Creates default search preferences based on the user's profile
     *
     * @param user User entity
     * @return Default SearchPreference
     */
    private SearchPreference createDefaultPreferences(User user) {
        return searchPreferenceRepository.save(SearchPreference.defaultForUser(user));
    }

    /**
     * Resets search preferences to default for a user
     *
     * @param userId User ID
     * @return Default SearchPreferenceDto
     */
    public SearchPreferenceDto resetToDefault(Long userId) {
        log.info("Resetting search preferences to default for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        searchPreferenceRepository.deleteByUserId(userId);

        SearchPreference defaultPreference = createDefaultPreferences(user);
        return searchPreferenceMapper.toDto(defaultPreference);
    }
}

