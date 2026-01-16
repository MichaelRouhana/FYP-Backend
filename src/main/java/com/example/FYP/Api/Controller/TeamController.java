package com.example.FYP.Api.Controller;

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
@RequestMapping("/team")
@RequiredArgsConstructor
@Tag(name = "Team Controller", description = "Provides detailed team information")
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "Get team header information", description = "Returns basic team information including name, logo, country, stadium, coach, etc.")
    @GetMapping("/{id}/header")
    public ResponseEntity<TeamHeaderDTO> getTeamHeader(@PathVariable Long id) {
        TeamHeaderDTO header = teamService.getTeamHeader(id);
        return ResponseEntity.ok(header);
    }

    @Operation(summary = "Get last finished match", description = "Returns the most recent finished match for this team")
    @GetMapping("/{id}/last-match")
    public ResponseEntity<FixtureViewDTO> getLastMatch(@PathVariable Long id) {
        FixtureViewDTO lastMatch = teamService.getLastMatch(id);
        if (lastMatch == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lastMatch);
    }

    @Operation(summary = "Get team squad", description = "Returns the current squad/roster for the team, ordered by position")
    @GetMapping("/{id}/squad")
    public ResponseEntity<List<SquadMemberDTO>> getSquad(@PathVariable Long id) {
        List<SquadMemberDTO> squad = teamService.getSquad(id);
        return ResponseEntity.ok(squad);
    }

    @Operation(summary = "Get team statistics", description = "Returns team statistics for a specific league")
    @GetMapping("/{id}/statistics")
    public ResponseEntity<TeamStatsDTO> getStatistics(
            @PathVariable Long id,
            @RequestParam Long leagueId
    ) {
        TeamStatsDTO stats = teamService.getStatistics(id, leagueId);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get team trophies", description = "Returns list of trophies/honors won by the team")
    @GetMapping("/{id}/trophies")
    public ResponseEntity<List<TrophyDTO>> getTrophies(@PathVariable Long id) {
        List<TrophyDTO> trophies = teamService.getTrophies(id);
        return ResponseEntity.ok(trophies);
    }

    @Operation(summary = "Get team details", description = "Returns detailed team information including stadium details, city, capacity, and founded year")
    @GetMapping("/{teamId}/details")
    public ResponseEntity<TeamDetailsDTO> getTeamDetails(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeamDetails(teamId));
    }
}

