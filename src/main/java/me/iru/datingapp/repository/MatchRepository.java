package me.iru.datingapp.repository;

import me.iru.datingapp.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m WHERE (m.user1.id = :userId OR m.user2.id = :userId) AND m.isActive = true")
    List<Match> findActiveMatchesByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Match m WHERE m.user1.id = :userId OR m.user2.id = :userId")
    List<Match> findAllMatchesByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Match m WHERE " +
            "(m.user1.id = :userId1 AND m.user2.id = :userId2) OR " +
            "(m.user1.id = :userId2 AND m.user2.id = :userId1)")
    Optional<Match> findMatchBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Match m WHERE " +
            "(m.user1.id = :userId1 AND m.user2.id = :userId2) OR " +
            "(m.user1.id = :userId2 AND m.user2.id = :userId1)")
    boolean existsMatchBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT m FROM Match m WHERE " +
            "m.isActive = true AND " +
            "((m.user1.id = :userId1 AND m.user2.id = :userId2) OR " +
            "(m.user1.id = :userId2 AND m.user2.id = :userId1))")
    Optional<Match> findActiveMatchBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT COUNT(m) FROM Match m WHERE " +
            "(m.user1.id = :userId OR m.user2.id = :userId) AND m.isActive = true")
    Long countActiveMatchesByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Match m WHERE " +
            "(m.user1.id = :userId OR m.user2.id = :userId) AND m.isActive = :isActive")
    List<Match> findByUserIdAndIsActive(@Param("userId") Long userId, @Param("isActive") boolean isActive);
}

