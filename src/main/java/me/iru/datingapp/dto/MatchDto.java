package me.iru.datingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchDto {

    private Long id;
    private Long matchedUserId;
    private String matchedUserName;
    private String matchedUserPhotoUrl;
    private Integer matchedUserAge;
    private String matchedUserCity;
    private LocalDateTime matchedAt;
    private Boolean isActive;
    private MessageDto lastMessage;
}

