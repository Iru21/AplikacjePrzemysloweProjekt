package me.iru.datingapp.mapper;

import me.iru.datingapp.dto.RatingDto;
import me.iru.datingapp.entity.Rating;
import me.iru.datingapp.entity.User;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public RatingDto toDto(Rating rating) {
        if (rating == null) {
            return null;
        }

        RatingDto dto = new RatingDto();
        dto.setRatedUserId(rating.getRatedUser().getId());
        dto.setRatingType(rating.getRatingType());

        return dto;
    }

    public Rating toEntity(RatingDto dto, User rater, User ratedUser) {
        if (dto == null || rater == null || ratedUser == null) {
            return null;
        }

        Rating rating = new Rating();
        rating.setRater(rater);
        rating.setRatedUser(ratedUser);
        rating.setRatingType(dto.getRatingType());

        return rating;
    }
}

