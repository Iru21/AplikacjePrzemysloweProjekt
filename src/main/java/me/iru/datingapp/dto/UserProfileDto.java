package me.iru.datingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.iru.datingapp.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private User.Gender gender;
    private Integer age;
    private String city;
    private String bio;
    private String photoUrl;
    private LocalDateTime createdAt;
    private List<String> interests;
}

