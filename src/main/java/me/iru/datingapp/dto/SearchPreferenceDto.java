package me.iru.datingapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.iru.datingapp.entity.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchPreferenceDto {

    private Long id;

    private User.Gender preferredGender;

    @NotNull(message = "Minimum age is required")
    @Min(value = 18, message = "Minimum age must be at least 18")
    @Max(value = 120, message = "Minimum age must be less than 120")
    private Integer minAge;

    @NotNull(message = "Maximum age is required")
    @Min(value = 18, message = "Maximum age must be at least 18")
    @Max(value = 120, message = "Maximum age must be less than 120")
    private Integer maxAge;

    @Min(value = 1, message = "Maximum distance must be at least 1 km")
    @Max(value = 10000, message = "Maximum distance must be less than 10000 km")
    private Integer maxDistance;
}

