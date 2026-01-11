package me.iru.datingapp.controller.api;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.MatchDto;
import me.iru.datingapp.service.MatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private static final Logger log = LoggerFactory.getLogger(MatchController.class);

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<List<MatchDto>> getMatches(@RequestParam Long userId) {
        log.info("REST API: Get matches for user ID: {}", userId);
        List<MatchDto> matches = matchService.getActiveMatches(userId);
        return ResponseEntity.ok(matches);
    }

    @DeleteMapping("/{matchId}/unmatch")
    public ResponseEntity<Void> unmatch(
            @PathVariable Long matchId,
            @RequestParam Long userId) {
        log.info("REST API: User {} unmatching from match ID: {}", userId, matchId);
        matchService.unmatch(userId, matchId);
        return ResponseEntity.noContent().build();
    }
}

