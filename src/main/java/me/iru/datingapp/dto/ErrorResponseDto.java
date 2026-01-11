package me.iru.datingapp.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDto {

    private String message;
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String path;
    private Map<String, String> validationErrors;

    public ErrorResponseDto(String message, Integer status) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDto(String message, Integer status, String path) {
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}

