package me.iru.datingapp.mapper;

import me.iru.datingapp.dto.RatingDto;
import me.iru.datingapp.entity.Rating;
import me.iru.datingapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RatingMapperTest {

    private RatingMapper ratingMapper;
    private User rater;
    private User ratedUser;
    private Rating testRating;

    @BeforeEach
    void setUp() {
        ratingMapper = new RatingMapper();

        rater = new User();
        rater.setId(1L);
        rater.setEmail("rater@example.com");
        rater.setFirstName("John");
        rater.setLastName("Doe");

        ratedUser = new User();
        ratedUser.setId(2L);
        ratedUser.setEmail("rated@example.com");
        ratedUser.setFirstName("Jane");
        ratedUser.setLastName("Smith");

        testRating = new Rating();
        testRating.setId(1L);
        testRating.setRater(rater);
        testRating.setRatedUser(ratedUser);
        testRating.setRatingType(Rating.RatingType.LIKE);
    }

    
    @Test
    void testToDto_Success_Like() {
                RatingDto result = ratingMapper.toDto(testRating);

                assertThat(result).isNotNull();
        assertThat(result.getRatedUserId()).isEqualTo(2L);
        assertThat(result.getRatingType()).isEqualTo(Rating.RatingType.LIKE);
    }

    @Test
    void testToDto_Success_Dislike() {
                testRating.setRatingType(Rating.RatingType.DISLIKE);

                RatingDto result = ratingMapper.toDto(testRating);

                assertThat(result).isNotNull();
        assertThat(result.getRatedUserId()).isEqualTo(2L);
        assertThat(result.getRatingType()).isEqualTo(Rating.RatingType.DISLIKE);
    }

    @Test
    void testToDto_WithNullRating() {
                RatingDto result = ratingMapper.toDto(null);

                assertThat(result).isNull();
    }

    @Test
    void testToDto_OnlyIncludesRatedUserId() {
                RatingDto result = ratingMapper.toDto(testRating);

                assertThat(result).isNotNull();
        assertThat(result.getRatedUserId()).isEqualTo(2L);
            }

    @Test
    void testToDto_DifferentUsers() {
                User user3 = new User();
        user3.setId(100L);
        testRating.setRatedUser(user3);

                RatingDto result = ratingMapper.toDto(testRating);

                assertThat(result.getRatedUserId()).isEqualTo(100L);
    }

    
    @Test
    void testToEntity_Success_Like() {
                RatingDto dto = new RatingDto();
        dto.setRatedUserId(2L);
        dto.setRatingType(Rating.RatingType.LIKE);

                Rating result = ratingMapper.toEntity(dto, rater, ratedUser);

                assertThat(result).isNotNull();
        assertThat(result.getRater()).isEqualTo(rater);
        assertThat(result.getRatedUser()).isEqualTo(ratedUser);
        assertThat(result.getRatingType()).isEqualTo(Rating.RatingType.LIKE);
        assertThat(result.getId()).isNull();
        assertThat(result.getCreatedAt()).isNull();
    }

    @Test
    void testToEntity_Success_Dislike() {
                RatingDto dto = new RatingDto();
        dto.setRatedUserId(2L);
        dto.setRatingType(Rating.RatingType.DISLIKE);

                Rating result = ratingMapper.toEntity(dto, rater, ratedUser);

                assertThat(result).isNotNull();
        assertThat(result.getRatingType()).isEqualTo(Rating.RatingType.DISLIKE);
    }

    @Test
    void testToEntity_WithNullDto() {
                Rating result = ratingMapper.toEntity(null, rater, ratedUser);

                assertThat(result).isNull();
    }

    @Test
    void testToEntity_WithNullRater() {
                RatingDto dto = new RatingDto();
        dto.setRatedUserId(2L);
        dto.setRatingType(Rating.RatingType.LIKE);

                Rating result = ratingMapper.toEntity(dto, null, ratedUser);

                assertThat(result).isNull();
    }

    @Test
    void testToEntity_WithNullRatedUser() {
                RatingDto dto = new RatingDto();
        dto.setRatedUserId(2L);
        dto.setRatingType(Rating.RatingType.LIKE);

                Rating result = ratingMapper.toEntity(dto, rater, null);

                assertThat(result).isNull();
    }

    @Test
    void testToEntity_WithAllNullParameters() {
                Rating result = ratingMapper.toEntity(null, null, null);

                assertThat(result).isNull();
    }

    @Test
    void testToEntity_SetsCorrectUsers() {
                RatingDto dto = new RatingDto();
        dto.setRatedUserId(2L);
        dto.setRatingType(Rating.RatingType.LIKE);

                Rating result = ratingMapper.toEntity(dto, rater, ratedUser);

                assertThat(result.getRater()).isNotNull();
        assertThat(result.getRater().getId()).isEqualTo(1L);
        assertThat(result.getRatedUser()).isNotNull();
        assertThat(result.getRatedUser().getId()).isEqualTo(2L);
    }

    @Test
    void testToEntity_DtoRatedUserIdNotUsed() {
                RatingDto dto = new RatingDto();
        dto.setRatedUserId(999L);
        dto.setRatingType(Rating.RatingType.LIKE);

                Rating result = ratingMapper.toEntity(dto, rater, ratedUser);

                assertThat(result.getRatedUser().getId()).isEqualTo(2L);
    }

    
    @Test
    void testRoundTrip_Like() {
                RatingDto dto = new RatingDto();
        dto.setRatedUserId(2L);
        dto.setRatingType(Rating.RatingType.LIKE);

                Rating entity = ratingMapper.toEntity(dto, rater, ratedUser);
        RatingDto result = ratingMapper.toDto(entity);

                assertThat(result).isNotNull();
        assertThat(result.getRatedUserId()).isEqualTo(2L);
        assertThat(result.getRatingType()).isEqualTo(Rating.RatingType.LIKE);
    }

    @Test
    void testRoundTrip_Dislike() {
                RatingDto dto = new RatingDto();
        dto.setRatedUserId(2L);
        dto.setRatingType(Rating.RatingType.DISLIKE);

                Rating entity = ratingMapper.toEntity(dto, rater, ratedUser);
        RatingDto result = ratingMapper.toDto(entity);

                assertThat(result).isNotNull();
        assertThat(result.getRatedUserId()).isEqualTo(2L);
        assertThat(result.getRatingType()).isEqualTo(Rating.RatingType.DISLIKE);
    }

    
    @Test
    void testToEntity_SameUserRatingThemselves() {
                RatingDto dto = new RatingDto();
        dto.setRatedUserId(1L);
        dto.setRatingType(Rating.RatingType.LIKE);

                Rating result = ratingMapper.toEntity(dto, rater, rater);

                assertThat(result).isNotNull();
        assertThat(result.getRater()).isEqualTo(rater);
        assertThat(result.getRatedUser()).isEqualTo(rater);
    }

    @Test
    void testToDto_BothRatingTypes() {
                testRating.setRatingType(Rating.RatingType.LIKE);
        RatingDto likeResult = ratingMapper.toDto(testRating);
        assertThat(likeResult.getRatingType()).isEqualTo(Rating.RatingType.LIKE);

                testRating.setRatingType(Rating.RatingType.DISLIKE);
        RatingDto dislikeResult = ratingMapper.toDto(testRating);
        assertThat(dislikeResult.getRatingType()).isEqualTo(Rating.RatingType.DISLIKE);
    }

    @Test
    void testToEntity_BothRatingTypes() {
        RatingDto likeDto = new RatingDto();
        likeDto.setRatingType(Rating.RatingType.LIKE);
        Rating likeEntity = ratingMapper.toEntity(likeDto, rater, ratedUser);
        assertThat(likeEntity.getRatingType()).isEqualTo(Rating.RatingType.LIKE);

        RatingDto dislikeDto = new RatingDto();
        dislikeDto.setRatingType(Rating.RatingType.DISLIKE);
        Rating dislikeEntity = ratingMapper.toEntity(dislikeDto, rater, ratedUser);
        assertThat(dislikeEntity.getRatingType()).isEqualTo(Rating.RatingType.DISLIKE);
    }

    @Test
    void testToEntity_MultipleUsers() {
                User user3 = new User();
        user3.setId(3L);

        User user4 = new User();
        user4.setId(4L);

        RatingDto dto = new RatingDto();
        dto.setRatingType(Rating.RatingType.LIKE);

                Rating result1 = ratingMapper.toEntity(dto, rater, ratedUser);
        Rating result2 = ratingMapper.toEntity(dto, user3, user4);

                assertThat(result1.getRater().getId()).isEqualTo(1L);
        assertThat(result1.getRatedUser().getId()).isEqualTo(2L);
        assertThat(result2.getRater().getId()).isEqualTo(3L);
        assertThat(result2.getRatedUser().getId()).isEqualTo(4L);
    }

    @Test
    void testToDto_PreservesRatingType() {
                testRating.setRatingType(Rating.RatingType.LIKE);

                RatingDto result = ratingMapper.toDto(testRating);

                assertThat(result.getRatingType()).isEqualTo(Rating.RatingType.LIKE);
    }

    @Test
    void testToEntity_PreservesRatingType() {
                RatingDto dto = new RatingDto();
        dto.setRatingType(Rating.RatingType.DISLIKE);

                Rating result = ratingMapper.toEntity(dto, rater, ratedUser);

                assertThat(result.getRatingType()).isEqualTo(Rating.RatingType.DISLIKE);
    }
}

