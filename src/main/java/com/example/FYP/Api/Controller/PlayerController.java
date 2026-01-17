package com.example.FYP.Api.Controller;

import com.example.FYP.Api.Model.View.Player.PlayerDetailedStatsDTO;
import com.example.FYP.Api.Service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/player")
@RequiredArgsConstructor
@Tag(name = "Player Controller", description = "Provides detailed player information and statistics")
public class PlayerController {

    private final PlayerService playerService;

    @Operation(summary = "Get player statistics", description = "Returns detailed statistics for a player including summary, attacking, passing, defending, and discipline stats")
    @GetMapping("/{id}/stats")
    public ResponseEntity<PlayerDetailedStatsDTO> getPlayerStats(
            @PathVariable Long id,
            @RequestParam(defaultValue = "2023") int season
    ) {
        PlayerDetailedStatsDTO stats = playerService.getPlayerStats(id, season);
        return ResponseEntity.ok(stats);
    }
}

