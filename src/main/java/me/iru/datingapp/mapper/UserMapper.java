package me.iru.datingapp.mapper;

import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.dto.UserRegistrationDto;
import me.iru.datingapp.dto.UserUpdateDto;
import me.iru.datingapp.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserProfileDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setGender(user.getGender());
        dto.setAge(user.getAge());
        dto.setCity(user.getCity());
        dto.setBio(user.getBio());
        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setCreatedAt(user.getCreatedAt());

        List<String> interests = user.getUserInterests() != null ?
                user.getUserInterests().stream()
                        .map(ui -> ui.getInterest().getName())
                        .collect(Collectors.toList()) :
                Collections.emptyList();
        dto.setInterests(interests);

        return dto;
    }


    public User toEntity(UserRegistrationDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setGender(dto.getGender());
        user.setAge(dto.getAge());
        user.setCity(dto.getCity());
        user.setBio(dto.getBio());
        user.setRole(User.Role.USER);

        return user;
    }


    public void updateEntityFromDto(UserUpdateDto dto, User user) {
        if (dto == null || user == null) {
            return;
        }

        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }

        if (dto.getCity() != null) {
            user.setCity(dto.getCity());
        }

        if (dto.getPhotoUrl() != null) {
            user.setPhotoUrl(dto.getPhotoUrl());
        }
    }


    public List<UserProfileDto> toDtoList(List<User> users) {
        if (users == null) {
            return Collections.emptyList();
        }

        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

