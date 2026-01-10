package me.iru.datingapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    private Long id;

    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotNull(message = "Match ID is required")
    private Long matchId;

    @NotBlank(message = "Message content is required")
    @Size(max = 5000, message = "Message content should not exceed 5000 characters")
    private String content;

    private LocalDateTime sentAt;

    private Boolean isRead;

    private String senderName;
    private String receiverName;
}

