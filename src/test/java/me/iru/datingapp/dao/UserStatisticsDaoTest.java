package me.iru.datingapp.dao;

import me.iru.datingapp.entity.Match;
import me.iru.datingapp.entity.Message;
import me.iru.datingapp.entity.Rating;
import me.iru.datingapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserStatisticsDaoTest {

    @Container
    static MariaDBContainer mariaDB = new MariaDBContainer("mariadb:latest")
            .withDatabaseName("testdb")
            .withReuse(true);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestEntityManager entityManager;

    private UserStatisticsDao userStatisticsDao;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        userStatisticsDao = new UserStatisticsDao(jdbcTemplate);

        jdbcTemplate.execute("DELETE FROM messages");
        jdbcTemplate.execute("DELETE FROM ratings");
        jdbcTemplate.execute("DELETE FROM matches");
        jdbcTemplate.execute("DELETE FROM users");

        user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setGender(User.Gender.MALE);
        user1.setAge(25);
        user1.setCity("Warsaw");
        user1 = entityManager.persistAndFlush(user1);

        user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setGender(User.Gender.FEMALE);
        user2.setAge(28);
        user2.setCity("Warsaw");
        user2 = entityManager.persistAndFlush(user2);

        user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setPassword("password3");
        user3.setFirstName("Bob");
        user3.setLastName("Johnson");
        user3.setGender(User.Gender.MALE);
        user3.setAge(30);
        user3.setCity("Krakow");
        user3 = entityManager.persistAndFlush(user3);
    }

    @Test
    void testGetUserStatistics_WithNoActivity() {
        UserStatistics stats = userStatisticsDao.getUserStatistics(user1.getId());

        assertThat(stats).isNotNull();
        assertThat(stats.getUserId()).isEqualTo(user1.getId());
        assertThat(stats.getEmail()).isEqualTo("user1@example.com");
        assertThat(stats.getFirstName()).isEqualTo("John");
        assertThat(stats.getLastName()).isEqualTo("Doe");
        assertThat(stats.getCity()).isEqualTo("Warsaw");
        assertThat(stats.getMatchCount()).isEqualTo(0);
        assertThat(stats.getMessagesSent()).isEqualTo(0);
        assertThat(stats.getMessagesReceived()).isEqualTo(0);
        assertThat(stats.getLikesGiven()).isEqualTo(0);
        assertThat(stats.getLikesReceived()).isEqualTo(0);
    }

    @Test
    void testGetUserStatistics_WithActivity() {
        Match match = new Match();
        match.setUser1(user1);
        match.setUser2(user2);
        match.setIsActive(true);
        match = entityManager.persistAndFlush(match);

        Message message1 = new Message();
        message1.setSender(user1);
        message1.setReceiver(user2);
        message1.setMatch(match);
        message1.setContent("Hello");
        message1.setIsRead(false);
        entityManager.persistAndFlush(message1);

        Message message2 = new Message();
        message2.setSender(user2);
        message2.setReceiver(user1);
        message2.setMatch(match);
        message2.setContent("Hi");
        message2.setIsRead(false);
        entityManager.persistAndFlush(message2);

        Rating like1 = new Rating();
        like1.setRater(user1);
        like1.setRatedUser(user2);
        like1.setRatingType(Rating.RatingType.LIKE);
        entityManager.persistAndFlush(like1);

        Rating like2 = new Rating();
        like2.setRater(user2);
        like2.setRatedUser(user1);
        like2.setRatingType(Rating.RatingType.LIKE);
        entityManager.persistAndFlush(like2);

        entityManager.clear();

        UserStatistics stats = userStatisticsDao.getUserStatistics(user1.getId());

        assertThat(stats).isNotNull();
        assertThat(stats.getMatchCount()).isEqualTo(1);
        assertThat(stats.getMessagesSent()).isEqualTo(1);
        assertThat(stats.getMessagesReceived()).isEqualTo(1);
        assertThat(stats.getLikesGiven()).isEqualTo(1);
        assertThat(stats.getLikesReceived()).isEqualTo(1);
    }

    @Test
    void testRowMapper_MapsAllFields() {
        UserStatistics stats = userStatisticsDao.getUserStatistics(user1.getId());

        assertThat(stats.getUserId()).isNotNull();
        assertThat(stats.getEmail()).isNotNull();
        assertThat(stats.getFirstName()).isNotNull();
        assertThat(stats.getLastName()).isNotNull();
        assertThat(stats.getCity()).isNotNull();
        assertThat(stats.getMatchCount()).isNotNull();
        assertThat(stats.getMessagesSent()).isNotNull();
        assertThat(stats.getMessagesReceived()).isNotNull();
        assertThat(stats.getLikesGiven()).isNotNull();
        assertThat(stats.getLikesReceived()).isNotNull();
    }

    @Test
    void testGetAllUserStatistics() {
        List<UserStatistics> allStats = userStatisticsDao.getAllUserStatistics();

        assertThat(allStats).hasSize(3);
        assertThat(allStats).extracting(UserStatistics::getEmail)
                .contains("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    void testGetTopUsersByMatches() {
        Match match1 = new Match();
        match1.setUser1(user1);
        match1.setUser2(user2);
        match1.setIsActive(true);
        entityManager.persistAndFlush(match1);

        Match match2 = new Match();
        match2.setUser1(user1);
        match2.setUser2(user3);
        match2.setIsActive(true);
        entityManager.persistAndFlush(match2);

        entityManager.clear();

        List<UserStatistics> topUsers = userStatisticsDao.getTopUsersByMatches(2);

        assertThat(topUsers).hasSize(2);
        assertThat(topUsers.getFirst().getUserId()).isEqualTo(user1.getId());
        assertThat(topUsers.getFirst().getMatchCount()).isEqualTo(2);
    }

    @Test
    void testGetUserCountByCity() {
        List<UserStatisticsDao.CityCount> cityCounts = userStatisticsDao.getUserCountByCity();

        assertThat(cityCounts).isNotEmpty();
        assertThat(cityCounts).extracting(UserStatisticsDao.CityCount::city)
                .contains("Warsaw", "Krakow");

        UserStatisticsDao.CityCount warsawCount = cityCounts.stream()
                .filter(cc -> cc.city().equals("Warsaw"))
                .findFirst()
                .orElseThrow();
        assertThat(warsawCount.count()).isEqualTo(2);
    }

    @Test
    void testDeleteOldInactiveMatches() {
        Match inactiveMatch = new Match();
        inactiveMatch.setUser1(user1);
        inactiveMatch.setUser2(user2);
        inactiveMatch.setIsActive(false);
        entityManager.persistAndFlush(inactiveMatch);

        jdbcTemplate.update(
                "UPDATE matches SET matched_at = DATE_SUB(NOW(), INTERVAL 100 DAY) WHERE id = ?",
                inactiveMatch.getId()
        );

        entityManager.clear();

        int deletedCount = userStatisticsDao.deleteOldInactiveMatches(90);

        assertThat(deletedCount).isEqualTo(1);
    }

    @Test
    void testDeleteOldReadMessages() {
        Match match = new Match();
        match.setUser1(user1);
        match.setUser2(user2);
        match.setIsActive(true);
        match = entityManager.persistAndFlush(match);

        Message message = new Message();
        message.setSender(user1);
        message.setReceiver(user2);
        message.setMatch(match);
        message.setContent("Old message");
        message.setIsRead(true);
        message = entityManager.persistAndFlush(message);

        jdbcTemplate.update(
                "UPDATE messages SET sent_at = DATE_SUB(NOW(), INTERVAL 100 DAY) WHERE id = ?",
                message.getId()
        );

        entityManager.clear();

        int deletedCount = userStatisticsDao.deleteOldReadMessages(90);

        assertThat(deletedCount).isEqualTo(1);
    }

    @Test
    void testMarkAllMessagesAsRead() {
        Match match = new Match();
        match.setUser1(user1);
        match.setUser2(user2);
        match.setIsActive(true);
        match = entityManager.persistAndFlush(match);

        Message message1 = new Message();
        message1.setSender(user1);
        message1.setReceiver(user2);
        message1.setMatch(match);
        message1.setContent("Message 1");
        message1.setIsRead(false);
        entityManager.persistAndFlush(message1);

        Message message2 = new Message();
        message2.setSender(user1);
        message2.setReceiver(user2);
        message2.setMatch(match);
        message2.setContent("Message 2");
        message2.setIsRead(false);
        entityManager.persistAndFlush(message2);

        entityManager.clear();

        int updatedCount = userStatisticsDao.markAllMessagesAsRead(match.getId(), user2.getId());

        assertThat(updatedCount).isEqualTo(2);

        Long unreadCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM messages WHERE match_id = ? AND receiver_id = ? AND is_read = false",
                Long.class,
                match.getId(),
                user2.getId()
        );
        assertThat(unreadCount).isEqualTo(0);
    }

    @Test
    void testGetMatchActivityStats() {
        Match match = new Match();
        match.setUser1(user1);
        match.setUser2(user2);
        match.setIsActive(true);
        match = entityManager.persistAndFlush(match);

        Message message1 = new Message();
        message1.setSender(user1);
        message1.setReceiver(user2);
        message1.setMatch(match);
        message1.setContent("Message 1");
        message1.setIsRead(false);
        entityManager.persistAndFlush(message1);

        Message message2 = new Message();
        message2.setSender(user2);
        message2.setReceiver(user1);
        message2.setMatch(match);
        message2.setContent("Message 2");
        message2.setIsRead(false);
        entityManager.persistAndFlush(message2);

        entityManager.clear();

        List<Map<String, Object>> stats = userStatisticsDao.getMatchActivityStats();

        assertThat(stats).isNotEmpty();
        Map<String, Object> matchStats = stats.getFirst();
        assertThat(matchStats.get("match_id")).isEqualTo(match.getId());
        assertThat(matchStats.get("message_count")).isEqualTo(2L);
        assertThat(matchStats.get("last_message_at")).isNotNull();
    }

    @Test
    void testInsertOperation() {
        long initialCount = Objects.requireNonNull(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class));

        jdbcTemplate.update(
                "INSERT INTO users (email, password, first_name, last_name, gender, age, city, role, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                "newuser@example.com",
                "password",
                "New",
                "User",
                "MALE",
                25,
                "Gdansk",
                "USER"
        );

        long newCount = Objects.requireNonNull(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class));
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    @Test
    void testUpdateOperation() {
        String originalCity = user1.getCity();

        int updatedRows = jdbcTemplate.update(
                "UPDATE users SET city = ? WHERE id = ?",
                "Poznan",
                user1.getId()
        );

        assertThat(updatedRows).isEqualTo(1);
        String newCity = jdbcTemplate.queryForObject(
                "SELECT city FROM users WHERE id = ?",
                String.class,
                user1.getId()
        );
        assertThat(newCity).isEqualTo("Poznan");
        assertThat(newCity).isNotEqualTo(originalCity);
    }

    @Test
    void testDeleteOperation() {
        long initialCount = Objects.requireNonNull(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class));

        int deletedRows = jdbcTemplate.update(
                "DELETE FROM users WHERE id = ?",
                user3.getId()
        );

        assertThat(deletedRows).isEqualTo(1);
        long newCount = Objects.requireNonNull(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class));
        assertThat(newCount).isEqualTo(initialCount - 1);
    }
}

