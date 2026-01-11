package me.iru.datingapp.repository;

import me.iru.datingapp.entity.Match;
import me.iru.datingapp.entity.Message;
import me.iru.datingapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private User user3;
    private Match match1;
    private Match match2;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();

        user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        user1.setFirstName("User");
        user1.setLastName("One");
        user1.setGender(User.Gender.MALE);
        user1.setAge(25);
        user1.setCity("Warsaw");
        user1 = entityManager.persistAndFlush(user1);

        user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setGender(User.Gender.FEMALE);
        user2.setAge(28);
        user2.setCity("Warsaw");
        user2 = entityManager.persistAndFlush(user2);

        user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setPassword("password3");
        user3.setFirstName("User");
        user3.setLastName("Three");
        user3.setGender(User.Gender.FEMALE);
        user3.setAge(30);
        user3.setCity("Krakow");
        user3 = entityManager.persistAndFlush(user3);

        match1 = new Match();
        match1.setUser1(user1);
        match1.setUser2(user2);
        match1.setIsActive(true);
        match1 = entityManager.persistAndFlush(match1);

        match2 = new Match();
        match2.setUser1(user1);
        match2.setUser2(user3);
        match2.setIsActive(true);
        match2 = entityManager.persistAndFlush(match2);
    }

    @Test
    void testSaveMessage() {
        // Given
        Message message = new Message();
        message.setSender(user1);
        message.setReceiver(user2);
        message.setMatch(match1);
        message.setContent("Hello!");
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);

        assertThat(savedMessage).isNotNull();
        assertThat(savedMessage.getId()).isNotNull();
        assertThat(savedMessage.getContent()).isEqualTo("Hello!");
        assertThat(savedMessage.getSentAt()).isNotNull();
        assertThat(savedMessage.getIsRead()).isFalse();
    }

    @Test
    void testFindByMatchIdOrderBySentAtAsc() {
        Message message1 = new Message();
        message1.setSender(user1);
        message1.setReceiver(user2);
        message1.setMatch(match1);
        message1.setContent("First message");
        message1.setIsRead(false);
        messageRepository.save(message1);

        Message message2 = new Message();
        message2.setSender(user2);
        message2.setReceiver(user1);
        message2.setMatch(match1);
        message2.setContent("Second message");
        message2.setIsRead(false);
        messageRepository.save(message2);

        List<Message> messages = messageRepository.findByMatchIdOrderBySentAtAsc(match1.getId());

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getContent()).isEqualTo("First message");
        assertThat(messages.get(1).getContent()).isEqualTo("Second message");
    }

    @Test
    void testFindByMatchIdWithPagination() {
        for (int i = 1; i <= 5; i++) {
            Message message = new Message();
            message.setSender(user1);
            message.setReceiver(user2);
            message.setMatch(match1);
            message.setContent("Message " + i);
            message.setIsRead(false);
            messageRepository.save(message);
        }

        Pageable pageable = PageRequest.of(0, 3);

        Page<Message> messagesPage = messageRepository.findByMatchId(match1.getId(), pageable);

        assertThat(messagesPage.getContent()).hasSize(3);
        assertThat(messagesPage.getTotalElements()).isEqualTo(5);
        assertThat(messagesPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testFindMessagesBetweenUsers() {
        Message message1 = new Message();
        message1.setSender(user1);
        message1.setReceiver(user2);
        message1.setMatch(match1);
        message1.setContent("User1 to User2");
        message1.setIsRead(false);
        messageRepository.save(message1);

        Message message2 = new Message();
        message2.setSender(user2);
        message2.setReceiver(user1);
        message2.setMatch(match1);
        message2.setContent("User2 to User1");
        message2.setIsRead(false);
        messageRepository.save(message2);

        Message message3 = new Message();
        message3.setSender(user1);
        message3.setReceiver(user3);
        message3.setMatch(match2);
        message3.setContent("User1 to User3");
        message3.setIsRead(false);
        messageRepository.save(message3);

        List<Message> messages = messageRepository.findMessagesBetweenUsers(user1.getId(), user2.getId());

        assertThat(messages).hasSize(2);
    }

    @Test
    void testFindBySenderId() {
        Message message1 = new Message();
        message1.setSender(user1);
        message1.setReceiver(user2);
        message1.setMatch(match1);
        message1.setContent("Message 1");
        message1.setIsRead(false);
        messageRepository.save(message1);

        Message message2 = new Message();
        message2.setSender(user1);
        message2.setReceiver(user3);
        message2.setMatch(match2);
        message2.setContent("Message 2");
        message2.setIsRead(false);
        messageRepository.save(message2);

        List<Message> messages = messageRepository.findBySenderId(user1.getId());

        assertThat(messages).hasSize(2);
    }

    @Test
    void testFindByReceiverId() {
        Message message1 = new Message();
        message1.setSender(user1);
        message1.setReceiver(user2);
        message1.setMatch(match1);
        message1.setContent("Message to user2");
        message1.setIsRead(false);
        messageRepository.save(message1);

        Message message2 = new Message();
        message2.setSender(user3);
        message2.setReceiver(user2);
        message2.setMatch(match1);
        message2.setContent("Another message to user2");
        message2.setIsRead(false);
        messageRepository.save(message2);

        List<Message> messages = messageRepository.findByReceiverId(user2.getId());

        assertThat(messages).hasSize(2);
    }

    @Test
    void testFindUnreadMessagesByUserId() {
        Message readMessage = new Message();
        readMessage.setSender(user1);
        readMessage.setReceiver(user2);
        readMessage.setMatch(match1);
        readMessage.setContent("Read message");
        readMessage.setIsRead(true);
        messageRepository.save(readMessage);

        Message unreadMessage = new Message();
        unreadMessage.setSender(user1);
        unreadMessage.setReceiver(user2);
        unreadMessage.setMatch(match1);
        unreadMessage.setContent("Unread message");
        unreadMessage.setIsRead(false);
        messageRepository.save(unreadMessage);

        List<Message> unreadMessages = messageRepository.findUnreadMessagesByUserId(user2.getId());

        assertThat(unreadMessages).hasSize(1);
        assertThat(unreadMessages.getFirst().getContent()).isEqualTo("Unread message");
    }

    @Test
    void testCountUnreadMessagesByUserId() {
        Message message1 = new Message();
        message1.setSender(user1);
        message1.setReceiver(user2);
        message1.setMatch(match1);
        message1.setContent("Unread 1");
        message1.setIsRead(false);
        messageRepository.save(message1);

        Message message2 = new Message();
        message2.setSender(user1);
        message2.setReceiver(user2);
        message2.setMatch(match1);
        message2.setContent("Unread 2");
        message2.setIsRead(false);
        messageRepository.save(message2);

        Message message3 = new Message();
        message3.setSender(user1);
        message3.setReceiver(user2);
        message3.setMatch(match1);
        message3.setContent("Read");
        message3.setIsRead(true);
        messageRepository.save(message3);

        Long count = messageRepository.countUnreadMessagesByUserId(user2.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountByMatchId() {
        for (int i = 1; i <= 3; i++) {
            Message message = new Message();
            message.setSender(user1);
            message.setReceiver(user2);
            message.setMatch(match1);
            message.setContent("Message " + i);
            message.setIsRead(false);
            messageRepository.save(message);
        }

        Long count = messageRepository.countByMatchId(match1.getId());

        assertThat(count).isEqualTo(3);
    }

    @Test
    void testFindByMatchIdOrderBySentAtDesc() throws InterruptedException {
        Message message1 = new Message();
        message1.setSender(user1);
        message1.setReceiver(user2);
        message1.setMatch(match1);
        message1.setContent("First");
        message1.setIsRead(false);
        messageRepository.save(message1);
        entityManager.flush();

        Thread.sleep(10);

        Message message2 = new Message();
        message2.setSender(user2);
        message2.setReceiver(user1);
        message2.setMatch(match1);
        message2.setContent("Last");
        message2.setIsRead(false);
        messageRepository.save(message2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> messagesPage = messageRepository.findByMatchIdOrderBySentAtDesc(match1.getId(), pageable);

        assertThat(messagesPage.getContent()).hasSize(2);
        assertThat(messagesPage.getContent().getFirst().getContent()).isEqualTo("Last");
    }

    @Test
    void testUpdateMessageReadStatus() {
        Message message = new Message();
        message.setSender(user1);
        message.setReceiver(user2);
        message.setMatch(match1);
        message.setContent("Test message");
        message.setIsRead(false);
        Message savedMessage = messageRepository.save(message);

        savedMessage.setIsRead(true);
        messageRepository.save(savedMessage);

        Message updatedMessage = messageRepository.findById(savedMessage.getId()).orElseThrow();
        assertThat(updatedMessage.getIsRead()).isTrue();
    }

    @Test
    void testDeleteMessage() {
        Message message = new Message();
        message.setSender(user1);
        message.setReceiver(user2);
        message.setMatch(match1);
        message.setContent("Test message");
        message.setIsRead(false);
        Message savedMessage = messageRepository.save(message);

        messageRepository.deleteById(savedMessage.getId());

        assertThat(messageRepository.findById(savedMessage.getId())).isEmpty();
    }
}

