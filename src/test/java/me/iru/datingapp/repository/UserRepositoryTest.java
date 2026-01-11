package me.iru.datingapp.repository;

import me.iru.datingapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser1 = new User();
        testUser1.setEmail("john.doe@example.com");
        testUser1.setPassword("hashedPassword123");
        testUser1.setFirstName("John");
        testUser1.setLastName("Doe");
        testUser1.setGender(User.Gender.MALE);
        testUser1.setAge(25);
        testUser1.setCity("Warsaw");
        testUser1.setBio("Software developer");
        testUser1.setPhotoUrl("/uploads/john.jpg");

        testUser2 = new User();
        testUser2.setEmail("jane.smith@example.com");
        testUser2.setPassword("hashedPassword456");
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setGender(User.Gender.FEMALE);
        testUser2.setAge(28);
        testUser2.setCity("Warsaw");
        testUser2.setBio("Designer");
        testUser2.setPhotoUrl("/uploads/jane.jpg");

        testUser3 = new User();
        testUser3.setEmail("alex.johnson@example.com");
        testUser3.setPassword("hashedPassword789");
        testUser3.setFirstName("Alex");
        testUser3.setLastName("Johnson");
        testUser3.setGender(User.Gender.OTHER);
        testUser3.setAge(30);
        testUser3.setCity("Krakow");
        testUser3.setBio("Artist");
    }

    @Test
    void testSaveUser() {
        User savedUser = userRepository.save(testUser1);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    void testFindById() {
        User savedUser = userRepository.save(testUser1);

        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(foundUser.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void testFindByEmail() {
        userRepository.save(testUser1);

        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFirstName()).isEqualTo("John");
        assertThat(foundUser.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    void testFindByGenderAndAgeBetween() {
        userRepository.save(testUser1); // Male, 25
        userRepository.save(testUser2); // Female, 28
        userRepository.save(testUser3); // Other, 30

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> femaleUsers = userRepository.findByGenderAndAgeBetween(User.Gender.FEMALE, 20, 30, pageable);

        assertThat(femaleUsers).isNotEmpty();
        assertThat(femaleUsers.getContent()).hasSize(1);
        assertThat(femaleUsers.getContent().getFirst().getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    void testUpdateUser() {
        User savedUser = userRepository.save(testUser1);
        Long userId = savedUser.getId();

        savedUser.setBio("Updated bio - Senior developer");
        savedUser.setCity("Gdansk");
        userRepository.save(savedUser);

        Optional<User> updatedUser = userRepository.findById(userId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getBio()).isEqualTo("Updated bio - Senior developer");
        assertThat(updatedUser.get().getCity()).isEqualTo("Gdansk");
    }

    @Test
    void testDeleteUser() {
        User savedUser = userRepository.save(testUser1);
        Long userId = savedUser.getId();

        userRepository.deleteById(userId);

        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void testExistsByEmail() {
        userRepository.save(testUser1);

        boolean exists = userRepository.existsByEmail("john.doe@example.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void testFindAllWithPagination() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);

        Pageable pageable = PageRequest.of(0, 2);

        Page<User> page = userRepository.findAll(pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testCustomQueryFindBySearchCriteria() {
        userRepository.save(testUser1); // Male, 25, Warsaw
        userRepository.save(testUser2); // Female, 28, Warsaw
        userRepository.save(testUser3); // Other, 30, Kraków

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> results = userRepository.findBySearchCriteria(null, 20, 30, "Warsaw", pageable);

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent())
                .extracting(User::getCity)
                .containsOnly("Warsaw");
    }

    @Test
    void testCascadeDeleteWithRelations() {
        User savedUser = userRepository.save(testUser1);
        Long userId = savedUser.getId();

        userRepository.deleteById(userId);
        userRepository.flush();

        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void testFindAllExcludingUser() {
        User user1 = userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> results = userRepository.findAllExcludingUser(user1.getId(), pageable);

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent())
                .extracting(User::getId)
                .doesNotContain(user1.getId());
    }

    @Test
    void testFindMatchingUsers() {
        User user1 = userRepository.save(testUser1); // Male, 25
        userRepository.save(testUser2); // Female, 28
        userRepository.save(testUser3); // Other, 30

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> results = userRepository.findMatchingUsers(
                user1.getId(),
                User.Gender.FEMALE,
                25,
                30,
                pageable
        );

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().getFirst().getGender()).isEqualTo(User.Gender.FEMALE);
        assertThat(results.getContent().getFirst().getAge()).isBetween(25, 30);
    }

    @Test
    void testFindByCity() {
        userRepository.save(testUser1); // Warsaw
        userRepository.save(testUser2); // Warsaw
        userRepository.save(testUser3); // Krakow

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> warsawUsers = userRepository.findByCity("Warsaw", pageable);

        assertThat(warsawUsers.getContent()).hasSize(2);
        assertThat(warsawUsers.getContent())
                .extracting(User::getCity)
                .containsOnly("Warsaw");
    }
}

