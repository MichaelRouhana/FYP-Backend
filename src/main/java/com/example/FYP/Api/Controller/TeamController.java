package com.example.FYP.Api.Controller;

import com.example.FYP.Api.Loader.Annotation.Feature;
import com.example.FYP.Api.Model.View.FixtureViewDTO;
import com.example.FYP.Api.Model.View.Team.*;
import com.example.FYP.Api.Service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Team Controller", description = "Provides detailed team information")
@Feature
public class TeamController {

    private final TeamService teamService;

    // Test endpoint to verify controller is registered
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("TeamController is working!");
    }

    @Operation(summary = "Get team header information", description = "Returns basic team information including name, logo, country, stadium, coach, etc.")
    @GetMapping("/{teamId}/header")
    public ResponseEntity<TeamHeaderDTO> getHeader(@PathVariable Long teamId) {
        TeamHeaderDTO header = teamService.getTeamHeader(teamId);
        return ResponseEntity.ok(header);
    }

    @Operation(summary = "Get team standings", description = "Returns standings for a team in a specific league/season")
    @GetMapping("/{teamId}/standings")
    public ResponseEntity<List<StandingRowDTO>> getTeamStandings(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "2023") int season,
            @RequestParam(defaultValue = "39") int league
    ) {
        // For now, use the existing method (league parameter can be used later when implementing full standings)
        List<StandingRowDTO> standings = teamService.getStandings(teamId, season);
        return ResponseEntity.ok(standings);
    }

    @Operation(summary = "Get team trophies", description = "Returns list of trophies/honors won by the team")
    @GetMapping("/{teamId}/trophies")
    public ResponseEntity<List<TrophyDTO>> getTeamTrophies(@PathVariable Long teamId) {
        List<TrophyDTO> trophies = teamService.getTrophies(teamId);
        return ResponseEntity.ok(trophies);
    }

    // Additional endpoints for backward compatibility
    @Operation(summary = "Get last finished match", description = "Returns the most recent finished match for this team")
    @GetMapping("/{teamId}/last-match")
    public ResponseEntity<FixtureViewDTO> getLastMatch(@PathVariable Long teamId) {
        FixtureViewDTO lastMatch = teamService.getLastMatch(teamId);
        if (lastMatch == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lastMatch);
    }

    @Operation(summary = "Get team squad", description = "Returns the current squad/roster for the team, ordered by position")
    @GetMapping("/{teamId}/squad")
    public ResponseEntity<List<SquadMemberDTO>> getSquad(@PathVariable Long teamId) {
        List<SquadMemberDTO> squad = teamService.getSquad(teamId);
        return ResponseEntity.ok(squad);
    }

    @Operation(summary = "Get team statistics", description = "Returns team statistics for a specific league")
    @GetMapping("/{teamId}/statistics")
    public ResponseEntity<TeamStatsDTO> getStatistics(
            @PathVariable Long teamId,
            @RequestParam Long leagueId
    ) {
        TeamStatsDTO stats = teamService.getStatistics(teamId, leagueId);
        return ResponseEntity.ok(stats);
    }
}
