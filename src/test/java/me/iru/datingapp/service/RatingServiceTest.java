package me.iru.datingapp.service;

import me.iru.datingapp.dto.RatingDto;
import me.iru.datingapp.entity.Match;
import me.iru.datingapp.entity.Rating;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.repository.MatchRepository;
import me.iru.datingapp.repository.RatingRepository;
import me.iru.datingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RatingService ratingService;

    private User user1;
    private User user2;
    private RatingDto ratingDto;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setFirstName("John");
        user1.setGender(User.Gender.MALE);

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setFirstName("Jane");
        user2.setGender(User.Gender.FEMALE);

        ratingDto = new RatingDto();
        ratingDto.setRatedUserId(2L);
        ratingDto.setRatingType(Rating.RatingType.LIKE);
    }

    @Test
    void testRateUser_Success_Like() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(ratingRepository.existsByRaterIdAndRatedUserId(1L, 2L)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenReturn(new Rating());
        when(ratingRepository.findByRaterIdAndRatedUserId(2L, 1L)).thenReturn(Optional.empty());

        ratingService.rateUser(1L, ratingDto);

        ArgumentCaptor<Rating> ratingCaptor = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(ratingCaptor.capture());
        Rating savedRating = ratingCaptor.getValue();

        assertThat(savedRating.getRater()).isEqualTo(user1);
        assertThat(savedRating.getRatedUser()).isEqualTo(user2);
        assertThat(savedRating.getRatingType()).isEqualTo(Rating.RatingType.LIKE);
    }

    @Test
    void testRateUser_Success_Dislike() {
        ratingDto.setRatingType(Rating.RatingType.DISLIKE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(ratingRepository.existsByRaterIdAndRatedUserId(1L, 2L)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenReturn(new Rating());

        ratingService.rateUser(1L, ratingDto);

        verify(ratingRepository).save(any(Rating.class));
        verify(ratingRepository, never()).findByRaterIdAndRatedUserId(anyLong(), anyLong());
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void testRateUser_RaterNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.rateUser(1L, ratingDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 1");

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void testRateUser_RatedUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.rateUser(1L, ratingDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 2");

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void testRateUser_AlreadyRated() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(ratingRepository.existsByRaterIdAndRatedUserId(1L, 2L)).thenReturn(true);

        ratingService.rateUser(1L, ratingDto);

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void testMutualLike_CreatesMatch() {
        Rating reciprocalRating = new Rating();
        reciprocalRating.setRater(user2);
        reciprocalRating.setRatedUser(user1);
        reciprocalRating.setRatingType(Rating.RatingType.LIKE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(ratingRepository.existsByRaterIdAndRatedUserId(1L, 2L)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenReturn(new Rating());
        when(ratingRepository.findByRaterIdAndRatedUserId(2L, 1L)).thenReturn(Optional.of(reciprocalRating));
        when(matchRepository.existsMatchBetweenUsers(1L, 2L)).thenReturn(false);
        when(matchRepository.save(any(Match.class))).thenReturn(new Match());

        ratingService.rateUser(1L, ratingDto);

        ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(matchCaptor.capture());
        Match savedMatch = matchCaptor.getValue();

        assertThat(savedMatch.getUser1()).isEqualTo(user1);
        assertThat(savedMatch.getUser2()).isEqualTo(user2);
        assertThat(savedMatch.getIsActive()).isTrue();
    }

    @Test
    void testMutualLike_MatchAlreadyExists() {
        Rating reciprocalRating = new Rating();
        reciprocalRating.setRater(user2);
        reciprocalRating.setRatedUser(user1);
        reciprocalRating.setRatingType(Rating.RatingType.LIKE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(ratingRepository.existsByRaterIdAndRatedUserId(1L, 2L)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenReturn(new Rating());
        when(ratingRepository.findByRaterIdAndRatedUserId(2L, 1L)).thenReturn(Optional.of(reciprocalRating));
        when(matchRepository.existsMatchBetweenUsers(1L, 2L)).thenReturn(true);

        ratingService.rateUser(1L, ratingDto);

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void testLike_NoReciprocalLike_NoMatch() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(ratingRepository.existsByRaterIdAndRatedUserId(1L, 2L)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenReturn(new Rating());
        when(ratingRepository.findByRaterIdAndRatedUserId(2L, 1L)).thenReturn(Optional.empty());

        ratingService.rateUser(1L, ratingDto);

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void testLike_ReciprocalDislike_NoMatch() {
        Rating reciprocalRating = new Rating();
        reciprocalRating.setRater(user2);
        reciprocalRating.setRatedUser(user1);
        reciprocalRating.setRatingType(Rating.RatingType.DISLIKE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(ratingRepository.existsByRaterIdAndRatedUserId(1L, 2L)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenReturn(new Rating());
        when(ratingRepository.findByRaterIdAndRatedUserId(2L, 1L)).thenReturn(Optional.of(reciprocalRating));

        ratingService.rateUser(1L, ratingDto);

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void testGetRating_Found() {
        Rating rating = new Rating();
        rating.setRater(user1);
        rating.setRatedUser(user2);
        rating.setRatingType(Rating.RatingType.LIKE);

        when(ratingRepository.findByRaterIdAndRatedUserId(1L, 2L))
                .thenReturn(Optional.of(rating));

        Optional<Rating> result = ratingService.getRating(1L, 2L);

        assertThat(result).isPresent();
        assertThat(result.get().getRatingType()).isEqualTo(Rating.RatingType.LIKE);
        verify(ratingRepository).findByRaterIdAndRatedUserId(1L, 2L);
    }

    @Test
    void testGetRating_NotFound() {
        when(ratingRepository.findByRaterIdAndRatedUserId(1L, 2L))
                .thenReturn(Optional.empty());

        Optional<Rating> result = ratingService.getRating(1L, 2L);

        assertThat(result).isEmpty();
        verify(ratingRepository).findByRaterIdAndRatedUserId(1L, 2L);
    }

    @Test
    void testHasMutualLike_True() {
        when(ratingRepository.existsMutualLike(1L, 2L)).thenReturn(true);

        boolean result = ratingService.hasMutualLike(1L, 2L);

        assertThat(result).isTrue();
        verify(ratingRepository).existsMutualLike(1L, 2L);
    }

    @Test
    void testHasMutualLike_False() {
        when(ratingRepository.existsMutualLike(1L, 2L)).thenReturn(false);

        boolean result = ratingService.hasMutualLike(1L, 2L);

        assertThat(result).isFalse();
        verify(ratingRepository).existsMutualLike(1L, 2L);
    }

    @Test
    void testDeleteRating_Success() {
        Rating rating = new Rating();
        rating.setRater(user1);
        rating.setRatedUser(user2);

        when(ratingRepository.findByRaterIdAndRatedUserId(1L, 2L))
                .thenReturn(Optional.of(rating));
        doNothing().when(ratingRepository).delete(rating);

        ratingService.deleteRating(1L, 2L);

        verify(ratingRepository).delete(rating);
    }
}

