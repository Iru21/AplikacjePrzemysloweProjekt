package me.iru.datingapp.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.RatingDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.service.MatchingService;
import me.iru.datingapp.service.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private static final Logger log = LoggerFactory.getLogger(MatchingController.class);

    private final MatchingService matchingService;
    private final RatingService ratingService;

    @GetMapping("/suggestions")
    public ResponseEntity<Page<UserProfileDto>> getSuggestions(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST API: Get suggestions for user ID: {}, page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfileDto> suggestions = matchingService.getSuggestedUsers(userId, pageable);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/rate")
    public ResponseEntity<Void> rateUser(
            @RequestParam Long userId,
            @Valid @RequestBody RatingDto ratingDto) {
        log.info("REST API: User {} rating user {}", userId, ratingDto.getRatedUserId());
        ratingService.rateUser(userId, ratingDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

