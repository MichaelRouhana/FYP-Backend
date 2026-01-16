package com.example.FYP.Api.Controller;

import com.example.FYP.Api.Model.View.Team.TeamHeaderDTO;
import com.example.FYP.Api.Model.View.Team.TeamStatsDTO;
import com.example.FYP.Api.Model.View.Team.TrophyDTO;
import com.example.FYP.Api.Service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// CRITICAL FIX: Must match the frontend's "/api/v1" prefix
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    // Matches GET /api/v1/team/{id}/header
    @GetMapping("/{teamId}/header")
    public ResponseEntity<TeamHeaderDTO> getTeamHeader(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getHeader(teamId));
    }

    // Matches GET /api/v1/team/{id}/standings
    @GetMapping("/{teamId}/standings")
    public ResponseEntity<List<TeamStatsDTO>> getTeamStandings(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "2023") int season,
            @RequestParam(defaultValue = "39") int league
    ) {
        return ResponseEntity.ok(teamService.getStandings(teamId, season, league));
    }

    // Matches GET /api/v1/team/{id}/trophies
    @GetMapping("/{teamId}/trophies")
    public ResponseEntity<List<TrophyDTO>> getTeamTrophies(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTrophies(teamId));
    }
}
