package me.iru.datingapp.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.MatchDto;
import me.iru.datingapp.service.MatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Matches", description = "Match management endpoints")
@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private static final Logger log = LoggerFactory.getLogger(MatchController.class);

    private final MatchService matchService;

    @Operation(summary = "Get user matches", description = "Get all active matches for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matches retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MatchDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    public ResponseEntity<List<MatchDto>> getMatches(
            @Parameter(description = "User ID") @RequestParam Long userId) {
        log.info("REST API: Get matches for user ID: {}", userId);
        List<MatchDto> matches = matchService.getActiveMatches(userId);
        return ResponseEntity.ok(matches);
    }

    @Operation(summary = "Unmatch", description = "Remove match and delete all associated messages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Unmatch successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Match not found")
    })
    @DeleteMapping("/{matchId}/unmatch")
    public ResponseEntity<Void> unmatch(
            @Parameter(description = "Match ID") @PathVariable Long matchId,
            @Parameter(description = "User ID") @RequestParam Long userId) {
        log.info("REST API: User {} unmatching from match ID: {}", userId, matchId);
        matchService.unmatch(userId, matchId);
        return ResponseEntity.noContent().build();
    }
}
