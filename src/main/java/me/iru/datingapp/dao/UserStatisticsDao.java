package me.iru.datingapp.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserStatisticsDao {

    private final JdbcTemplate jdbcTemplate;

    public UserStatistics getUserStatistics(Long userId) {
        String sql = """
                SELECT
                    u.id AS user_id,
                    u.email,
                    u.first_name,
                    u.last_name,
                    u.city,
                    COUNT(DISTINCT m.id) AS match_count,
                    COUNT(DISTINCT msg_sent.id) AS messages_sent,
                    COUNT(DISTINCT msg_received.id) AS messages_received,
                    COUNT(DISTINCT r_given.id) AS likes_given,
                    COUNT(DISTINCT r_received.id) AS likes_received
                FROM users u
                LEFT JOIN matches m ON (m.user1_id = u.id OR m.user2_id = u.id) AND m.is_active = true
                LEFT JOIN messages msg_sent ON msg_sent.sender_id = u.id
                LEFT JOIN messages msg_received ON msg_received.receiver_id = u.id
                LEFT JOIN ratings r_given ON r_given.rater_id = u.id AND r_given.rating_type = 'LIKE'
                LEFT JOIN ratings r_received ON r_received.rated_user_id = u.id AND r_received.rating_type = 'LIKE'
                WHERE u.id = ?
                GROUP BY u.id, u.email, u.first_name, u.last_name, u.city
                """;

        return jdbcTemplate.queryForObject(sql, new UserStatisticsRowMapper(), userId);
    }


    public List<UserStatistics> getAllUserStatistics() {
        String sql = """
                SELECT
                    u.id AS user_id,
                    u.email,
                    u.first_name,
                    u.last_name,
                    u.city,
                    COUNT(DISTINCT m.id) AS match_count,
                    COUNT(DISTINCT msg_sent.id) AS messages_sent,
                    COUNT(DISTINCT msg_received.id) AS messages_received,
                    COUNT(DISTINCT r_given.id) AS likes_given,
                    COUNT(DISTINCT r_received.id) AS likes_received
                FROM users u
                LEFT JOIN matches m ON (m.user1_id = u.id OR m.user2_id = u.id) AND m.is_active = true
                LEFT JOIN messages msg_sent ON msg_sent.sender_id = u.id
                LEFT JOIN messages msg_received ON msg_received.receiver_id = u.id
                LEFT JOIN ratings r_given ON r_given.rater_id = u.id AND r_given.rating_type = 'LIKE'
                LEFT JOIN ratings r_received ON r_received.rated_user_id = u.id AND r_received.rating_type = 'LIKE'
                GROUP BY u.id, u.email, u.first_name, u.last_name, u.city
                ORDER BY match_count DESC, likes_received DESC
                """;

        return jdbcTemplate.query(sql, new UserStatisticsRowMapper());
    }

    public List<UserStatistics> getTopUsersByMatches(int limit) {
        String sql = """
                SELECT
                    u.id AS user_id,
                    u.email,
                    u.first_name,
                    u.last_name,
                    u.city,
                    COUNT(DISTINCT m.id) AS match_count,
                    COUNT(DISTINCT msg_sent.id) AS messages_sent,
                    COUNT(DISTINCT msg_received.id) AS messages_received,
                    COUNT(DISTINCT r_given.id) AS likes_given,
                    COUNT(DISTINCT r_received.id) AS likes_received
                FROM users u
                LEFT JOIN matches m ON (m.user1_id = u.id OR m.user2_id = u.id) AND m.is_active = true
                LEFT JOIN messages msg_sent ON msg_sent.sender_id = u.id
                LEFT JOIN messages msg_received ON msg_received.receiver_id = u.id
                LEFT JOIN ratings r_given ON r_given.rater_id = u.id AND r_given.rating_type = 'LIKE'
                LEFT JOIN ratings r_received ON r_received.rated_user_id = u.id AND r_received.rating_type = 'LIKE'
                GROUP BY u.id, u.email, u.first_name, u.last_name, u.city
                ORDER BY match_count DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, new UserStatisticsRowMapper(), limit);
    }

    public List<CityCount> getUserCountByCity() {
        String sql = """
                SELECT city, COUNT(*) as user_count
                FROM users
                WHERE city IS NOT NULL
                GROUP BY city
                ORDER BY user_count DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new CityCount(rs.getString("city"), rs.getLong("user_count"))
        );
    }

    public int deleteOldInactiveMatches(int daysOld) {
        String sql = """
                DELETE FROM matches
                WHERE is_active = false
                AND matched_at < DATE_SUB(NOW(), INTERVAL ? DAY)
                """;

        return jdbcTemplate.update(sql, daysOld);
    }

    public int deleteOldReadMessages(int daysOld) {
        String sql = """
                DELETE FROM messages
                WHERE is_read = true
                AND sent_at < DATE_SUB(NOW(), INTERVAL ? DAY)
                """;

        return jdbcTemplate.update(sql, daysOld);
    }

    public int markAllMessagesAsRead(Long matchId, Long userId) {
        String sql = """
                UPDATE messages
                SET is_read = true
                WHERE match_id = ?
                AND receiver_id = ?
                AND is_read = false
                """;

        return jdbcTemplate.update(sql, matchId, userId);
    }


    public List<Map<String, Object>> getMatchActivityStats() {
        String sql = """
                SELECT
                    m.id as match_id,
                    COUNT(msg.id) as message_count,
                    MAX(msg.sent_at) as last_message_at
                FROM matches m
                LEFT JOIN messages msg ON msg.match_id = m.id
                WHERE m.is_active = true
                GROUP BY m.id
                ORDER BY message_count DESC
                """;

        return jdbcTemplate.queryForList(sql);
    }

    private static class UserStatisticsRowMapper implements RowMapper<UserStatistics> {
        @Override
        public UserStatistics mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserStatistics stats = new UserStatistics();
            stats.setUserId(rs.getLong("user_id"));
            stats.setEmail(rs.getString("email"));
            stats.setFirstName(rs.getString("first_name"));
            stats.setLastName(rs.getString("last_name"));
            stats.setCity(rs.getString("city"));
            stats.setMatchCount(rs.getLong("match_count"));
            stats.setMessagesSent(rs.getLong("messages_sent"));
            stats.setMessagesReceived(rs.getLong("messages_received"));
            stats.setLikesGiven(rs.getLong("likes_given"));
            stats.setLikesReceived(rs.getLong("likes_received"));
            return stats;
        }
    }

    public record CityCount(String city, Long count) {}
}

