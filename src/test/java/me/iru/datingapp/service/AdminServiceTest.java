package me.iru.datingapp.service;

import me.iru.datingapp.entity.User;
import me.iru.datingapp.repository.MatchRepository;
import me.iru.datingapp.repository.MessageRepository;
import me.iru.datingapp.repository.RatingRepository;
import me.iru.datingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private AdminService adminService;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setEmail("user1@example.com");
        testUser1.setFirstName("John");
        testUser1.setLastName("Doe");
        testUser1.setGender(User.Gender.MALE);
        testUser1.setAge(25);

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setEmail("user2@example.com");
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setGender(User.Gender.FEMALE);
        testUser2.setAge(28);

        testUser3 = new User();
        testUser3.setId(3L);
        testUser3.setEmail("user3@example.com");
        testUser3.setFirstName("Alex");
        testUser3.setLastName("Johnson");
        testUser3.setGender(User.Gender.OTHER);
        testUser3.setAge(30);
    }


    @Test
    void testGetPlatformStatistics_Success() {
        when(userRepository.count()).thenReturn(100L);
        when(matchRepository.count()).thenReturn(50L);
        when(messageRepository.count()).thenReturn(500L);
        when(ratingRepository.count()).thenReturn(200L);

        Map<String, Long> result = adminService.getPlatformStatistics();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result.get("totalUsers")).isEqualTo(100L);
        assertThat(result.get("totalMatches")).isEqualTo(50L);
        assertThat(result.get("totalMessages")).isEqualTo(500L);
        assertThat(result.get("totalRatings")).isEqualTo(200L);

        verify(userRepository).count();
        verify(matchRepository).count();
        verify(messageRepository).count();
        verify(ratingRepository).count();
    }

    @Test
    void testGetPlatformStatistics_WithZeroValues() {
        when(userRepository.count()).thenReturn(0L);
        when(matchRepository.count()).thenReturn(0L);
        when(messageRepository.count()).thenReturn(0L);
        when(ratingRepository.count()).thenReturn(0L);

        Map<String, Long> result = adminService.getPlatformStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalUsers")).isEqualTo(0L);
        assertThat(result.get("totalMatches")).isEqualTo(0L);
        assertThat(result.get("totalMessages")).isEqualTo(0L);
        assertThat(result.get("totalRatings")).isEqualTo(0L);

        verify(userRepository).count();
        verify(matchRepository).count();
        verify(messageRepository).count();
        verify(ratingRepository).count();
    }

    @Test
    void testGetPlatformStatistics_WithLargeNumbers() {
        when(userRepository.count()).thenReturn(10000L);
        when(matchRepository.count()).thenReturn(5000L);
        when(messageRepository.count()).thenReturn(100000L);
        when(ratingRepository.count()).thenReturn(50000L);

        Map<String, Long> result = adminService.getPlatformStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalUsers")).isEqualTo(10000L);
        assertThat(result.get("totalMatches")).isEqualTo(5000L);
        assertThat(result.get("totalMessages")).isEqualTo(100000L);
        assertThat(result.get("totalRatings")).isEqualTo(50000L);
    }

    @Test
    void testGetPlatformStatistics_ContainsAllRequiredKeys() {
        when(userRepository.count()).thenReturn(10L);
        when(matchRepository.count()).thenReturn(5L);
        when(messageRepository.count()).thenReturn(20L);
        when(ratingRepository.count()).thenReturn(15L);

        Map<String, Long> result = adminService.getPlatformStatistics();

        assertThat(result).containsKeys("totalUsers", "totalMatches", "totalMessages", "totalRatings");
    }


    @Test
    void testGetUserStatistics_Success() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByGender(User.Gender.MALE)).thenReturn(45L);
        when(userRepository.countByGender(User.Gender.FEMALE)).thenReturn(50L);
        when(userRepository.countByGender(User.Gender.OTHER)).thenReturn(5L);

        Map<String, Long> result = adminService.getUserStatistics();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result.get("totalUsers")).isEqualTo(100L);
        assertThat(result.get("maleUsers")).isEqualTo(45L);
        assertThat(result.get("femaleUsers")).isEqualTo(50L);
        assertThat(result.get("otherGenderUsers")).isEqualTo(5L);

        verify(userRepository).count();
        verify(userRepository).countByGender(User.Gender.MALE);
        verify(userRepository).countByGender(User.Gender.FEMALE);
        verify(userRepository).countByGender(User.Gender.OTHER);
    }

    @Test
    void testGetUserStatistics_AllMale() {
        when(userRepository.count()).thenReturn(50L);
        when(userRepository.countByGender(User.Gender.MALE)).thenReturn(50L);
        when(userRepository.countByGender(User.Gender.FEMALE)).thenReturn(0L);
        when(userRepository.countByGender(User.Gender.OTHER)).thenReturn(0L);

        Map<String, Long> result = adminService.getUserStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalUsers")).isEqualTo(50L);
        assertThat(result.get("maleUsers")).isEqualTo(50L);
        assertThat(result.get("femaleUsers")).isEqualTo(0L);
        assertThat(result.get("otherGenderUsers")).isEqualTo(0L);
    }

    @Test
    void testGetUserStatistics_AllFemale() {
        when(userRepository.count()).thenReturn(60L);
        when(userRepository.countByGender(User.Gender.MALE)).thenReturn(0L);
        when(userRepository.countByGender(User.Gender.FEMALE)).thenReturn(60L);
        when(userRepository.countByGender(User.Gender.OTHER)).thenReturn(0L);

        Map<String, Long> result = adminService.getUserStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalUsers")).isEqualTo(60L);
        assertThat(result.get("femaleUsers")).isEqualTo(60L);
        assertThat(result.get("maleUsers")).isEqualTo(0L);
    }

    @Test
    void testGetUserStatistics_NoUsers() {
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countByGender(User.Gender.MALE)).thenReturn(0L);
        when(userRepository.countByGender(User.Gender.FEMALE)).thenReturn(0L);
        when(userRepository.countByGender(User.Gender.OTHER)).thenReturn(0L);

        Map<String, Long> result = adminService.getUserStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalUsers")).isEqualTo(0L);
        assertThat(result.get("maleUsers")).isEqualTo(0L);
        assertThat(result.get("femaleUsers")).isEqualTo(0L);
        assertThat(result.get("otherGenderUsers")).isEqualTo(0L);
    }

    @Test
    void testGetUserStatistics_ContainsAllRequiredKeys() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByGender(User.Gender.MALE)).thenReturn(4L);
        when(userRepository.countByGender(User.Gender.FEMALE)).thenReturn(5L);
        when(userRepository.countByGender(User.Gender.OTHER)).thenReturn(1L);

        Map<String, Long> result = adminService.getUserStatistics();

        assertThat(result).containsKeys("totalUsers", "maleUsers", "femaleUsers", "otherGenderUsers");
    }

    @Test
    void testGetUserStatistics_GenderDistribution() {
        when(userRepository.count()).thenReturn(1000L);
        when(userRepository.countByGender(User.Gender.MALE)).thenReturn(480L);
        when(userRepository.countByGender(User.Gender.FEMALE)).thenReturn(510L);
        when(userRepository.countByGender(User.Gender.OTHER)).thenReturn(10L);

        Map<String, Long> result = adminService.getUserStatistics();

        assertThat(result).isNotNull();
        long totalByGender = result.get("maleUsers") + result.get("femaleUsers") + result.get("otherGenderUsers");
        assertThat(totalByGender).isEqualTo(result.get("totalUsers"));
    }


    @Test
    void testGetMatchStatistics_Success() {
        when(matchRepository.count()).thenReturn(100L);
        when(matchRepository.countByIsActive(true)).thenReturn(80L);
        when(matchRepository.countByIsActive(false)).thenReturn(20L);

        Map<String, Long> result = adminService.getMatchStatistics();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get("totalMatches")).isEqualTo(100L);
        assertThat(result.get("activeMatches")).isEqualTo(80L);
        assertThat(result.get("inactiveMatches")).isEqualTo(20L);

        verify(matchRepository).count();
        verify(matchRepository).countByIsActive(true);
        verify(matchRepository).countByIsActive(false);
    }

    @Test
    void testGetMatchStatistics_AllActive() {
        when(matchRepository.count()).thenReturn(50L);
        when(matchRepository.countByIsActive(true)).thenReturn(50L);
        when(matchRepository.countByIsActive(false)).thenReturn(0L);

        Map<String, Long> result = adminService.getMatchStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalMatches")).isEqualTo(50L);
        assertThat(result.get("activeMatches")).isEqualTo(50L);
        assertThat(result.get("inactiveMatches")).isEqualTo(0L);
    }

    @Test
    void testGetMatchStatistics_AllInactive() {
        when(matchRepository.count()).thenReturn(30L);
        when(matchRepository.countByIsActive(true)).thenReturn(0L);
        when(matchRepository.countByIsActive(false)).thenReturn(30L);

        Map<String, Long> result = adminService.getMatchStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalMatches")).isEqualTo(30L);
        assertThat(result.get("activeMatches")).isEqualTo(0L);
        assertThat(result.get("inactiveMatches")).isEqualTo(30L);
    }

    @Test
    void testGetMatchStatistics_NoMatches() {
        when(matchRepository.count()).thenReturn(0L);
        when(matchRepository.countByIsActive(true)).thenReturn(0L);
        when(matchRepository.countByIsActive(false)).thenReturn(0L);

        Map<String, Long> result = adminService.getMatchStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalMatches")).isEqualTo(0L);
        assertThat(result.get("activeMatches")).isEqualTo(0L);
        assertThat(result.get("inactiveMatches")).isEqualTo(0L);
    }

    @Test
    void testGetMatchStatistics_ContainsAllRequiredKeys() {
        when(matchRepository.count()).thenReturn(10L);
        when(matchRepository.countByIsActive(true)).thenReturn(7L);
        when(matchRepository.countByIsActive(false)).thenReturn(3L);

        Map<String, Long> result = adminService.getMatchStatistics();

        assertThat(result).containsKeys("totalMatches", "activeMatches", "inactiveMatches");
    }

    @Test
    void testGetMatchStatistics_ActivePlusInactiveEqualsTotal() {
        when(matchRepository.count()).thenReturn(200L);
        when(matchRepository.countByIsActive(true)).thenReturn(150L);
        when(matchRepository.countByIsActive(false)).thenReturn(50L);

        Map<String, Long> result = adminService.getMatchStatistics();

        assertThat(result).isNotNull();
        long totalByStatus = result.get("activeMatches") + result.get("inactiveMatches");
        assertThat(totalByStatus).isEqualTo(result.get("totalMatches"));
    }


    @Test
    void testGetActivityStatistics_Success() {
        when(messageRepository.count()).thenReturn(1000L);
        when(ratingRepository.count()).thenReturn(500L);

        Map<String, Long> result = adminService.getActivityStatistics();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get("totalMessages")).isEqualTo(1000L);
        assertThat(result.get("totalRatings")).isEqualTo(500L);

        verify(messageRepository).count();
        verify(ratingRepository).count();
    }

    @Test
    void testGetActivityStatistics_NoActivity() {
        when(messageRepository.count()).thenReturn(0L);
        when(ratingRepository.count()).thenReturn(0L);

        Map<String, Long> result = adminService.getActivityStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalMessages")).isEqualTo(0L);
        assertThat(result.get("totalRatings")).isEqualTo(0L);
    }

    @Test
    void testGetActivityStatistics_HighVolume() {
        when(messageRepository.count()).thenReturn(1000000L);
        when(ratingRepository.count()).thenReturn(500000L);

        Map<String, Long> result = adminService.getActivityStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalMessages")).isEqualTo(1000000L);
        assertThat(result.get("totalRatings")).isEqualTo(500000L);
    }

    @Test
    void testGetActivityStatistics_OnlyMessages() {
        when(messageRepository.count()).thenReturn(500L);
        when(ratingRepository.count()).thenReturn(0L);

        Map<String, Long> result = adminService.getActivityStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalMessages")).isEqualTo(500L);
        assertThat(result.get("totalRatings")).isEqualTo(0L);
    }

    @Test
    void testGetActivityStatistics_OnlyRatings() {
        when(messageRepository.count()).thenReturn(0L);
        when(ratingRepository.count()).thenReturn(300L);

        Map<String, Long> result = adminService.getActivityStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalMessages")).isEqualTo(0L);
        assertThat(result.get("totalRatings")).isEqualTo(300L);
    }

    @Test
    void testGetActivityStatistics_ContainsAllRequiredKeys() {
        when(messageRepository.count()).thenReturn(100L);
        when(ratingRepository.count()).thenReturn(50L);

        Map<String, Long> result = adminService.getActivityStatistics();

        assertThat(result).containsKeys("totalMessages", "totalRatings");
    }


    @Test
    void testGetAllUsers_Success() {
        List<User> users = List.of(testUser1, testUser2, testUser3);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = adminService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(testUser1, testUser2, testUser3);
        assertThat(result.get(0).getEmail()).isEqualTo("user1@example.com");
        assertThat(result.get(1).getEmail()).isEqualTo("user2@example.com");
        assertThat(result.get(2).getEmail()).isEqualTo("user3@example.com");

        verify(userRepository).findAll();
    }

    @Test
    void testGetAllUsers_EmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = adminService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(userRepository).findAll();
    }

    @Test
    void testGetAllUsers_SingleUser() {
        when(userRepository.findAll()).thenReturn(List.of(testUser1));

        List<User> result = adminService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(testUser1);
    }

    @Test
    void testGetAllUsers_MultipleGenders() {
        List<User> users = List.of(testUser1, testUser2, testUser3);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = adminService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.stream().filter(u -> u.getGender() == User.Gender.MALE).count()).isEqualTo(1);
        assertThat(result.stream().filter(u -> u.getGender() == User.Gender.FEMALE).count()).isEqualTo(1);
        assertThat(result.stream().filter(u -> u.getGender() == User.Gender.OTHER).count()).isEqualTo(1);
    }

    @Test
    void testGetAllUsers_LargeDataset() {
        when(userRepository.findAll()).thenReturn(List.of(testUser1, testUser2, testUser3));

        List<User> result = adminService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        verify(userRepository).findAll();
    }

    @Test
    void testGetAllUsers_ReturnsActualUserObjects() {
        when(userRepository.findAll()).thenReturn(List.of(testUser1));

        List<User> result = adminService.getAllUsers();

        assertThat(result.getFirst()).isNotNull();
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getFirstName()).isEqualTo("John");
        assertThat(result.getFirst().getLastName()).isEqualTo("Doe");
        assertThat(result.getFirst().getAge()).isEqualTo(25);
    }


    @Test
    void testAllStatisticsMethods_ReturnMaps() {
        when(userRepository.count()).thenReturn(10L);
        when(matchRepository.count()).thenReturn(5L);
        when(messageRepository.count()).thenReturn(20L);
        when(ratingRepository.count()).thenReturn(15L);
        when(userRepository.countByGender(any())).thenReturn(0L);
        when(matchRepository.countByIsActive(anyBoolean())).thenReturn(0L);

        Map<String, Long> platformStats = adminService.getPlatformStatistics();
        Map<String, Long> userStats = adminService.getUserStatistics();
        Map<String, Long> matchStats = adminService.getMatchStatistics();
        Map<String, Long> activityStats = adminService.getActivityStatistics();

        assertThat(platformStats).isInstanceOf(Map.class);
        assertThat(userStats).isInstanceOf(Map.class);
        assertThat(matchStats).isInstanceOf(Map.class);
        assertThat(activityStats).isInstanceOf(Map.class);
    }

    @Test
    void testStatisticsMethods_NonNull() {
        when(userRepository.count()).thenReturn(1L);
        when(matchRepository.count()).thenReturn(1L);
        when(messageRepository.count()).thenReturn(1L);
        when(ratingRepository.count()).thenReturn(1L);
        when(userRepository.countByGender(any())).thenReturn(0L);
        when(matchRepository.countByIsActive(anyBoolean())).thenReturn(0L);

        assertThat(adminService.getPlatformStatistics()).isNotNull();
        assertThat(adminService.getUserStatistics()).isNotNull();
        assertThat(adminService.getMatchStatistics()).isNotNull();
        assertThat(adminService.getActivityStatistics()).isNotNull();
        assertThat(adminService.getAllUsers()).isNotNull();
    }

    @Test
    void testPlatformStatistics_ConsistentCounts() {
        when(userRepository.count()).thenReturn(50L);
        when(matchRepository.count()).thenReturn(25L);
        when(messageRepository.count()).thenReturn(200L);
        when(ratingRepository.count()).thenReturn(100L);

        Map<String, Long> result = adminService.getPlatformStatistics();

        assertThat(result.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(375L);
    }

    @Test
    void testRepositoryInteractions_CalledExactlyOnce() {
        when(userRepository.count()).thenReturn(1L);
        when(matchRepository.count()).thenReturn(1L);
        when(messageRepository.count()).thenReturn(1L);
        when(ratingRepository.count()).thenReturn(1L);

        adminService.getPlatformStatistics();

        verify(userRepository, times(1)).count();
        verify(matchRepository, times(1)).count();
        verify(messageRepository, times(1)).count();
        verify(ratingRepository, times(1)).count();
    }
}

