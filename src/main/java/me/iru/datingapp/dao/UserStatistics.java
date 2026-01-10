package me.iru.datingapp.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Long matchCount;
    private Long messagesSent;
    private Long messagesReceived;
    private Long likesGiven;
    private Long likesReceived;
    private String city;
}

