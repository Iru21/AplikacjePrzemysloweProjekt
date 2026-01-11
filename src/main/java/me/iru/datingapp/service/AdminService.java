package me.iru.datingapp.service;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.repository.MatchRepository;
import me.iru.datingapp.repository.MessageRepository;
import me.iru.datingapp.repository.RatingRepository;
import me.iru.datingapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;
    private final RatingRepository ratingRepository;

    @Transactional(readOnly = true)
    public Map<String, Long> getPlatformStatistics() {
        log.debug("Fetching platform statistics");

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalMatches", matchRepository.count());
        stats.put("totalMessages", messageRepository.count());
        stats.put("totalRatings", ratingRepository.count());

        log.debug("Platform statistics retrieved: {}", stats);
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getUserStatistics() {
        log.debug("Fetching user statistics");

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("maleUsers", userRepository.countByGender(User.Gender.MALE));
        stats.put("femaleUsers", userRepository.countByGender(User.Gender.FEMALE));
        stats.put("otherGenderUsers", userRepository.countByGender(User.Gender.OTHER));

        log.debug("User statistics retrieved: {}", stats);
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getMatchStatistics() {
        log.debug("Fetching match statistics");

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalMatches", matchRepository.count());
        stats.put("activeMatches", matchRepository.countByIsActive(true));
        stats.put("inactiveMatches", matchRepository.countByIsActive(false));

        log.debug("Match statistics retrieved: {}", stats);
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getActivityStatistics() {
        log.debug("Fetching activity statistics");

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalMessages", messageRepository.count());
        stats.put("totalRatings", ratingRepository.count());

        log.debug("Activity statistics retrieved: {}", stats);
        return stats;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.debug("Fetching all users for admin panel");
        return userRepository.findAll();
    }
}

