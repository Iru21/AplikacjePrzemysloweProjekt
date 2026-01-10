package me.iru.datingapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.iru.datingapp.entity.Rating;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RatingDto {

    @NotNull(message = "Rated user ID is required")
    private Long ratedUserId;

    @NotNull(message = "Rating type is required")
    private Rating.RatingType ratingType;
}

