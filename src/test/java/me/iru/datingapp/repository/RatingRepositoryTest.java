package me.iru.datingapp.repository;

import me.iru.datingapp.entity.Rating;
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
class RatingRepositoryTest {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        ratingRepository.deleteAll();

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
        user3.setGender(User.Gender.MALE);
        user3.setAge(30);
        user3.setCity("Krakow");
        user3 = entityManager.persistAndFlush(user3);
    }

    @Test
    void testSaveRating() {
        Rating rating = new Rating();
        rating.setRater(user1);
        rating.setRatedUser(user2);
        rating.setRatingType(Rating.RatingType.LIKE);

        Rating savedRating = ratingRepository.save(rating);

        assertThat(savedRating).isNotNull();
        assertThat(savedRating.getId()).isNotNull();
        assertThat(savedRating.getRatingType()).isEqualTo(Rating.RatingType.LIKE);
        assertThat(savedRating.getCreatedAt()).isNotNull();
    }

    @Test
    void testFindByRaterIdAndRatedUserId() {
        Rating rating = new Rating();
        rating.setRater(user1);
        rating.setRatedUser(user2);
        rating.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(rating);

        Optional<Rating> foundRating = ratingRepository.findByRaterIdAndRatedUserId(user1.getId(), user2.getId());

        assertThat(foundRating).isPresent();
        assertThat(foundRating.get().getRatingType()).isEqualTo(Rating.RatingType.LIKE);
    }

    @Test
    void testExistsByRaterIdAndRatedUserId() {
        Rating rating = new Rating();
        rating.setRater(user1);
        rating.setRatedUser(user2);
        rating.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(rating);

        boolean exists = ratingRepository.existsByRaterIdAndRatedUserId(user1.getId(), user2.getId());
        boolean notExists = ratingRepository.existsByRaterIdAndRatedUserId(user1.getId(), user3.getId());

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void testFindByRaterId() {
        Rating rating1 = new Rating();
        rating1.setRater(user1);
        rating1.setRatedUser(user2);
        rating1.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(rating1);

        Rating rating2 = new Rating();
        rating2.setRater(user1);
        rating2.setRatedUser(user3);
        rating2.setRatingType(Rating.RatingType.DISLIKE);
        ratingRepository.save(rating2);

        List<Rating> ratings = ratingRepository.findByRaterId(user1.getId());

        assertThat(ratings).hasSize(2);
    }

    @Test
    void testFindLikesByRaterId() {
        Rating like = new Rating();
        like.setRater(user1);
        like.setRatedUser(user2);
        like.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(like);

        Rating dislike = new Rating();
        dislike.setRater(user1);
        dislike.setRatedUser(user3);
        dislike.setRatingType(Rating.RatingType.DISLIKE);
        ratingRepository.save(dislike);

        List<Rating> likes = ratingRepository.findLikesByRaterId(user1.getId());

        assertThat(likes).hasSize(1);
        assertThat(likes.getFirst().getRatingType()).isEqualTo(Rating.RatingType.LIKE);
    }

    @Test
    void testFindLikesReceivedByUserId() {
        Rating like1 = new Rating();
        like1.setRater(user1);
        like1.setRatedUser(user2);
        like1.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(like1);

        Rating like2 = new Rating();
        like2.setRater(user3);
        like2.setRatedUser(user2);
        like2.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(like2);

        List<Rating> likesReceived = ratingRepository.findLikesReceivedByUserId(user2.getId());

        assertThat(likesReceived).hasSize(2);
    }

    @Test
    void testExistsMutualLike() {
        Rating like1 = new Rating();
        like1.setRater(user1);
        like1.setRatedUser(user2);
        like1.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(like1);

        Rating like2 = new Rating();
        like2.setRater(user2);
        like2.setRatedUser(user1);
        like2.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(like2);

        boolean mutualLike = ratingRepository.existsMutualLike(user1.getId(), user2.getId());
        boolean noMutualLike = ratingRepository.existsMutualLike(user1.getId(), user3.getId());

        assertThat(mutualLike).isTrue();
        assertThat(noMutualLike).isFalse();
    }

    @Test
    void testFindMutualLikesForUser() {
        Rating like1 = new Rating();
        like1.setRater(user1);
        like1.setRatedUser(user2);
        like1.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(like1);

        Rating like2 = new Rating();
        like2.setRater(user2);
        like2.setRatedUser(user1);
        like2.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(like2);

        Rating like3 = new Rating();
        like3.setRater(user1);
        like3.setRatedUser(user3);
        like3.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(like3);

        List<Rating> mutualLikes = ratingRepository.findMutualLikesForUser(user1.getId());

        assertThat(mutualLikes).hasSize(1);
        assertThat(mutualLikes.getFirst().getRatedUser().getId()).isEqualTo(user2.getId());
    }

    @Test
    void testCountLikesReceivedByUser() {
        Rating like1 = new Rating();
        like1.setRater(user1);
        like1.setRatedUser(user2);
        like1.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(like1);

        Rating like2 = new Rating();
        like2.setRater(user3);
        like2.setRatedUser(user2);
        like2.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(like2);

        Long count = ratingRepository.countLikesReceivedByUser(user2.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountByRaterId() {
        Rating rating1 = new Rating();
        rating1.setRater(user1);
        rating1.setRatedUser(user2);
        rating1.setRatingType(Rating.RatingType.LIKE);
        ratingRepository.save(rating1);

        Rating rating2 = new Rating();
        rating2.setRater(user1);
        rating2.setRatedUser(user3);
        rating2.setRatingType(Rating.RatingType.DISLIKE);
        ratingRepository.save(rating2);

        Long count = ratingRepository.countByRaterId(user1.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void testDeleteRating() {
        Rating rating = new Rating();
        rating.setRater(user1);
        rating.setRatedUser(user2);
        rating.setRatingType(Rating.RatingType.LIKE);
        Rating savedRating = ratingRepository.save(rating);

        ratingRepository.deleteById(savedRating.getId());

        Optional<Rating> deletedRating = ratingRepository.findById(savedRating.getId());
        assertThat(deletedRating).isEmpty();
    }

    @Test
    void testUpdateRatingType() {
        Rating rating = new Rating();
        rating.setRater(user1);
        rating.setRatedUser(user2);
        rating.setRatingType(Rating.RatingType.LIKE);
        Rating savedRating = ratingRepository.save(rating);

        savedRating.setRatingType(Rating.RatingType.DISLIKE);
        ratingRepository.save(savedRating);

        Optional<Rating> updatedRating = ratingRepository.findById(savedRating.getId());
        assertThat(updatedRating).isPresent();
        assertThat(updatedRating.get().getRatingType()).isEqualTo(Rating.RatingType.DISLIKE);
    }
}

