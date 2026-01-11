package me.iru.datingapp.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Matching", description = "User matching and rating endpoints")
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private static final Logger log = LoggerFactory.getLogger(MatchingController.class);

    private final MatchingService matchingService;
    private final RatingService ratingService;

    @Operation(summary = "Get user suggestions", description = "Get paginated list of suggested users based on preferences")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suggestions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/suggestions")
    public ResponseEntity<Page<UserProfileDto>> getSuggestions(
            @Parameter(description = "Current user ID") @RequestParam Long userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("REST API: Get suggestions for user ID: {}, page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfileDto> suggestions = matchingService.getSuggestedUsers(userId, pageable);
        return ResponseEntity.ok(suggestions);
    }

    @Operation(summary = "Rate a user", description = "Rate a user with LIKE or DISLIKE. Mutual LIKE creates a match")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rating saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid rating data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/rate")
    public ResponseEntity<Void> rateUser(
            @Parameter(description = "Rater user ID") @RequestParam Long userId,
            @Valid @RequestBody RatingDto ratingDto) {
        log.info("REST API: User {} rating user {}", userId, ratingDto.getRatedUserId());
        ratingService.rateUser(userId, ratingDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
