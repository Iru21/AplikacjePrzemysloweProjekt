package me.iru.datingapp.repository;

import me.iru.datingapp.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    List<UserInterest> findByUserId(Long userId);

    List<UserInterest> findByInterestId(Long interestId);

    Optional<UserInterest> findByUserIdAndInterestId(Long userId, Long interestId);

    boolean existsByUserIdAndInterestId(Long userId, Long interestId);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserInterest ui WHERE ui.user.id = :userId AND ui.interest.id = :interestId")
    void deleteByUserIdAndInterestId(@Param("userId") Long userId, @Param("interestId") Long interestId);

    @Transactional
    void deleteByUserId(Long userId);
}
