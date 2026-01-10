package me.iru.datingapp.repository;

import me.iru.datingapp.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByMatchIdOrderBySentAtAsc(Long matchId);

    Page<Message> findByMatchId(Long matchId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
            "(m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
            "ORDER BY m.sentAt ASC")
    List<Message> findMessagesBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    List<Message> findBySenderId(Long senderId);

    List<Message> findByReceiverId(Long receiverId);

    @Query("SELECT m FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false")
    List<Message> findUnreadMessagesByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false")
    Long countUnreadMessagesByUserId(@Param("userId") Long userId);


    @Query("SELECT m FROM Message m WHERE m.match.id = :matchId ORDER BY m.sentAt DESC")
    Page<Message> findByMatchIdOrderBySentAtDesc(@Param("matchId") Long matchId, Pageable pageable);

    Long countByMatchId(Long matchId);

    @Query("SELECT m FROM Message m WHERE m.match.id = :matchId ORDER BY m.sentAt DESC")
    List<Message> findLatestMessageInMatch(@Param("matchId") Long matchId, Pageable pageable);
}

