package me.iru.datingapp.repository;

import me.iru.datingapp.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByRaterIdAndRatedUserId(Long raterId, Long ratedUserId);

    boolean existsByRaterIdAndRatedUserId(Long raterId, Long ratedUserId);

    List<Rating> findByRaterId(Long raterId);

    List<Rating> findByRatedUserId(Long ratedUserId);

    @Query("SELECT r FROM Rating r WHERE r.rater.id = :raterId AND r.ratingType = 'LIKE'")
    List<Rating> findLikesByRaterId(@Param("raterId") Long raterId);

    @Query("SELECT r FROM Rating r WHERE r.ratedUser.id = :ratedUserId AND r.ratingType = 'LIKE'")
    List<Rating> findLikesReceivedByUserId(@Param("ratedUserId") Long ratedUserId);

    @Query("SELECT CASE WHEN COUNT(r) = 2 THEN true ELSE false END " +
            "FROM Rating r WHERE " +
            "r.ratingType = 'LIKE' AND " +
            "((r.rater.id = :userId1 AND r.ratedUser.id = :userId2) OR " +
            "(r.rater.id = :userId2 AND r.ratedUser.id = :userId1))")
    boolean existsMutualLike(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT r FROM Rating r WHERE " +
            "r.rater.id = :userId AND " +
            "r.ratingType = 'LIKE' AND " +
            "EXISTS (SELECT r2 FROM Rating r2 WHERE " +
            "r2.rater.id = r.ratedUser.id AND " +
            "r2.ratedUser.id = :userId AND " +
            "r2.ratingType = 'LIKE')")
    List<Rating> findMutualLikesForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.ratedUser.id = :userId AND r.ratingType = 'LIKE'")
    Long countLikesReceivedByUser(@Param("userId") Long userId);

    Long countByRaterId(Long raterId);
}

