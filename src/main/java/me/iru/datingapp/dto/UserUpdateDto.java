package me.iru.datingapp.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    @Size(max = 1000, message = "Bio should not exceed 1000 characters")
    private String bio;

    @Size(max = 100, message = "City should not exceed 100 characters")
    private String city;

    private String photoUrl;

    private List<Long> interestIds;
}

