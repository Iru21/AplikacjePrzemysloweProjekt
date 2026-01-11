package me.iru.datingapp.repository;

import me.iru.datingapp.entity.Match;
import me.iru.datingapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MatchRepositoryTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        matchRepository.deleteAll();

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
    }

    @Test
    void testSaveMatch() {
        Match match = new Match();
        match.setUser1(user1);
        match.setUser2(user2);
        match.setIsActive(true);

        Match savedMatch = matchRepository.save(match);

        assertThat(savedMatch).isNotNull();
        assertThat(savedMatch.getId()).isNotNull();
        assertThat(savedMatch.getIsActive()).isTrue();
        assertThat(savedMatch.getMatchedAt()).isNotNull();
    }

    @Test
    void testFindActiveMatchesByUserId() {
        Match activeMatch = new Match();
        activeMatch.setUser1(user1);
        activeMatch.setUser2(user2);
        activeMatch.setIsActive(true);
        matchRepository.save(activeMatch);

        Match inactiveMatch = new Match();
        inactiveMatch.setUser1(user1);
        inactiveMatch.setUser2(user3);
        inactiveMatch.setIsActive(false);
        matchRepository.save(inactiveMatch);

        List<Match> activeMatches = matchRepository.findActiveMatchesByUserId(user1.getId());

        assertThat(activeMatches).hasSize(1);
        assertThat(activeMatches.getFirst().getIsActive()).isTrue();
    }

    @Test
    void testFindAllMatchesByUserId() {
        Match match1 = new Match();
        match1.setUser1(user1);
        match1.setUser2(user2);
        match1.setIsActive(true);
        matchRepository.save(match1);

        Match match2 = new Match();
        match2.setUser1(user1);
        match2.setUser2(user3);
        match2.setIsActive(false);
        matchRepository.save(match2);

        List<Match> allMatches = matchRepository.findAllMatchesByUserId(user1.getId());

        assertThat(allMatches).hasSize(2);
    }

    @Test
    void testFindMatchBetweenUsers() {
        Match match = new Match();
        match.setUser1(user1);
        match.setUser2(user2);
        match.setIsActive(true);
        matchRepository.save(match);

        Optional<Match> foundMatch1 = matchRepository.findMatchBetweenUsers(user1.getId(), user2.getId());
        Optional<Match> foundMatch2 = matchRepository.findMatchBetweenUsers(user2.getId(), user1.getId());

        assertThat(foundMatch1).isPresent();
        assertThat(foundMatch2).isPresent();
        assertThat(foundMatch1.get().getId()).isEqualTo(foundMatch2.get().getId());
    }

    @Test
    void testExistsMatchBetweenUsers() {
        Match match = new Match();
        match.setUser1(user1);
        match.setUser2(user2);
        match.setIsActive(true);
        matchRepository.save(match);

        boolean exists = matchRepository.existsMatchBetweenUsers(user1.getId(), user2.getId());
        boolean notExists = matchRepository.existsMatchBetweenUsers(user1.getId(), user3.getId());

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void testFindActiveMatchBetweenUsers() {
        Match activeMatch = new Match();
        activeMatch.setUser1(user1);
        activeMatch.setUser2(user2);
        activeMatch.setIsActive(true);
        matchRepository.save(activeMatch);

        Match inactiveMatch = new Match();
        inactiveMatch.setUser1(user1);
        inactiveMatch.setUser2(user3);
        inactiveMatch.setIsActive(false);
        matchRepository.save(inactiveMatch);

        Optional<Match> foundActive = matchRepository.findActiveMatchBetweenUsers(user1.getId(), user2.getId());
        Optional<Match> foundInactive = matchRepository.findActiveMatchBetweenUsers(user1.getId(), user3.getId());

        assertThat(foundActive).isPresent();
        assertThat(foundInactive).isEmpty();
    }

    @Test
    void testCountActiveMatchesByUserId() {
        Match match1 = new Match();
        match1.setUser1(user1);
        match1.setUser2(user2);
        match1.setIsActive(true);
        matchRepository.save(match1);

        Match match2 = new Match();
        match2.setUser1(user1);
        match2.setUser2(user3);
        match2.setIsActive(true);
        matchRepository.save(match2);

        Match inactiveMatch = new Match();
        inactiveMatch.setUser1(user2);
        inactiveMatch.setUser2(user3);
        inactiveMatch.setIsActive(false);
        matchRepository.save(inactiveMatch);

        Long count = matchRepository.countActiveMatchesByUserId(user1.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void testFindByUserIdAndIsActive() {
        // Given
        Match activeMatch = new Match();
        activeMatch.setUser1(user1);
        activeMatch.setUser2(user2);
        activeMatch.setIsActive(true);
        matchRepository.save(activeMatch);

        Match inactiveMatch = new Match();
        inactiveMatch.setUser1(user1);
        inactiveMatch.setUser2(user3);
        inactiveMatch.setIsActive(false);
        matchRepository.save(inactiveMatch);

        List<Match> activeMatches = matchRepository.findByUserIdAndIsActive(user1.getId(), true);
        List<Match> inactiveMatches = matchRepository.findByUserIdAndIsActive(user1.getId(), false);

        assertThat(activeMatches).hasSize(1);
        assertThat(inactiveMatches).hasSize(1);
    }

    @Test
    void testUpdateMatchActivity() {
        Match match = new Match();
        match.setUser1(user1);
        match.setUser2(user2);
        match.setIsActive(true);
        Match savedMatch = matchRepository.save(match);

        savedMatch.setIsActive(false);
        matchRepository.save(savedMatch);

        Optional<Match> updatedMatch = matchRepository.findById(savedMatch.getId());
        assertThat(updatedMatch).isPresent();
        assertThat(updatedMatch.get().getIsActive()).isFalse();
    }

    @Test
    void testDeleteMatch() {
        Match match = new Match();
        match.setUser1(user1);
        match.setUser2(user2);
        match.setIsActive(true);
        Match savedMatch = matchRepository.save(match);

        matchRepository.deleteById(savedMatch.getId());

        Optional<Match> deletedMatch = matchRepository.findById(savedMatch.getId());
        assertThat(deletedMatch).isEmpty();
    }

    @Test
    void testFindMatchesForUser2AsSecondUser() {
        Match match = new Match();
        match.setUser1(user1);
        match.setUser2(user2);
        match.setIsActive(true);
        matchRepository.save(match);

        List<Match> matches = matchRepository.findActiveMatchesByUserId(user2.getId());

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().getUser2().getId()).isEqualTo(user2.getId());
    }

    @Test
    void testMultipleActiveMatches() {
        Match match1 = new Match();
        match1.setUser1(user1);
        match1.setUser2(user2);
        match1.setIsActive(true);
        matchRepository.save(match1);

        Match match2 = new Match();
        match2.setUser1(user3);
        match2.setUser2(user1);
        match2.setIsActive(true);
        matchRepository.save(match2);

        List<Match> user1Matches = matchRepository.findActiveMatchesByUserId(user1.getId());

        assertThat(user1Matches).hasSize(2);
    }
}

