package me.iru.datingapp.repository;

import me.iru.datingapp.entity.Interest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InterestRepositoryTest {

    @Autowired
    private InterestRepository interestRepository;

    private Interest testInterest1;
    private Interest testInterest2;
    private Interest testInterest3;

    @BeforeEach
    void setUp() {
        interestRepository.deleteAll();

        testInterest1 = new Interest();
        testInterest1.setName("Programming");
        testInterest1.setDescription("Software development and coding");

        testInterest2 = new Interest();
        testInterest2.setName("Music");
        testInterest2.setDescription("Playing and listening to music");

        testInterest3 = new Interest();
        testInterest3.setName("Travel");
        testInterest3.setDescription("Exploring new places");
    }

    @Test
    void testSaveInterest() {
        Interest savedInterest = interestRepository.save(testInterest1);

        assertThat(savedInterest).isNotNull();
        assertThat(savedInterest.getId()).isNotNull();
        assertThat(savedInterest.getName()).isEqualTo("Programming");
        assertThat(savedInterest.getDescription()).isEqualTo("Software development and coding");
    }

    @Test
    void testFindById() {
        Interest savedInterest = interestRepository.save(testInterest1);

        Optional<Interest> foundInterest = interestRepository.findById(savedInterest.getId());

        assertThat(foundInterest).isPresent();
        assertThat(foundInterest.get().getName()).isEqualTo("Programming");
    }

    @Test
    void testFindByName() {
        interestRepository.save(testInterest1);

        Optional<Interest> foundInterest = interestRepository.findByName("Programming");

        assertThat(foundInterest).isPresent();
        assertThat(foundInterest.get().getDescription()).isEqualTo("Software development and coding");
    }

    @Test
    void testFindByName_NotFound() {
        Optional<Interest> foundInterest = interestRepository.findByName("NonExistent");

        assertThat(foundInterest).isEmpty();
    }

    @Test
    void testFindAllByOrderByNameAsc() {
        interestRepository.save(testInterest3);
        interestRepository.save(testInterest1);
        interestRepository.save(testInterest2);

        List<Interest> interests = interestRepository.findAllByOrderByNameAsc();

        assertThat(interests).hasSize(3);
        assertThat(interests.get(0).getName()).isEqualTo("Music");
        assertThat(interests.get(1).getName()).isEqualTo("Programming");
        assertThat(interests.get(2).getName()).isEqualTo("Travel");
    }

    @Test
    void testExistsByName() {
        interestRepository.save(testInterest1);

        boolean exists = interestRepository.existsByName("Programming");
        boolean notExists = interestRepository.existsByName("NonExistent");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void testUpdateInterest() {
        Interest savedInterest = interestRepository.save(testInterest1);
        Long interestId = savedInterest.getId();

        savedInterest.setDescription("Updated description - Advanced coding");
        interestRepository.save(savedInterest);

        Optional<Interest> updatedInterest = interestRepository.findById(interestId);
        assertThat(updatedInterest).isPresent();
        assertThat(updatedInterest.get().getDescription()).isEqualTo("Updated description - Advanced coding");
    }

    @Test
    void testDeleteInterest() {
        Interest savedInterest = interestRepository.save(testInterest1);
        Long interestId = savedInterest.getId();

        interestRepository.deleteById(interestId);

        Optional<Interest> deletedInterest = interestRepository.findById(interestId);
        assertThat(deletedInterest).isEmpty();
    }

    @Test
    void testFindAll() {
        interestRepository.save(testInterest1);
        interestRepository.save(testInterest2);
        interestRepository.save(testInterest3);

        List<Interest> allInterests = interestRepository.findAll();

        assertThat(allInterests).hasSize(3);
    }

    @Test
    void testCountInterests() {
        interestRepository.save(testInterest1);
        interestRepository.save(testInterest2);

        long count = interestRepository.count();

        assertThat(count).isEqualTo(2);
    }
}

