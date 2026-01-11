package me.iru.datingapp.controller.api;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.entity.Interest;
import me.iru.datingapp.service.InterestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private static final Logger log = LoggerFactory.getLogger(InterestController.class);

    private final InterestService interestService;

    @GetMapping
    public ResponseEntity<List<Interest>> getAllInterests() {
        log.info("REST API: Get all interests");
        List<Interest> interests = interestService.getAllInterests();
        return ResponseEntity.ok(interests);
    }

    @PostMapping("/users/{userId}/interests/{interestId}")
    public ResponseEntity<Void> addInterestToUser(
            @PathVariable Long userId,
            @PathVariable Long interestId) {
        log.info("REST API: Add interest {} to user {}", interestId, userId);
        interestService.addInterestToUser(userId, interestId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/users/{userId}/interests/{interestId}")
    public ResponseEntity<Void> removeInterestFromUser(
            @PathVariable Long userId,
            @PathVariable Long interestId) {
        log.info("REST API: Remove interest {} from user {}", interestId, userId);
        interestService.removeInterestFromUser(userId, interestId);
        return ResponseEntity.noContent().build();
    }
}

