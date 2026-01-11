package me.iru.datingapp.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.iru.datingapp.entity.Interest;
import me.iru.datingapp.service.InterestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Interests", description = "Interest management endpoints")
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private static final Logger log = LoggerFactory.getLogger(InterestController.class);

    private final InterestService interestService;

    @Operation(summary = "Get all interests", description = "Get list of all available interests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interests retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Interest.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<Interest>> getAllInterests() {
        log.info("REST API: Get all interests");
        List<Interest> interests = interestService.getAllInterests();
        return ResponseEntity.ok(interests);
    }

    @Operation(summary = "Add interest to user", description = "Add an interest to user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Interest added successfully"),
            @ApiResponse(responseCode = "400", description = "Interest already exists for user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User or interest not found")
    })
    @PostMapping("/users/{userId}/interests/{interestId}")
    public ResponseEntity<Void> addInterestToUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Interest ID") @PathVariable Long interestId) {
        log.info("REST API: Add interest {} to user {}", interestId, userId);
        interestService.addInterestToUser(userId, interestId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Remove interest from user", description = "Remove an interest from user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Interest removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User, interest, or association not found")
    })
    @DeleteMapping("/users/{userId}/interests/{interestId}")
    public ResponseEntity<Void> removeInterestFromUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Interest ID") @PathVariable Long interestId) {
        log.info("REST API: Remove interest {} from user {}", interestId, userId);
        interestService.removeInterestFromUser(userId, interestId);
        return ResponseEntity.noContent().build();
    }
}
