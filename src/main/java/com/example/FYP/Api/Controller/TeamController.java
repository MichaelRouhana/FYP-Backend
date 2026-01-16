package com.example.FYP.Api.Controller;

import com.example.FYP.Api.Model.View.Team.TeamHeaderDTO;
import com.example.FYP.Api.Model.View.Team.StandingRowDTO;
import com.example.FYP.Api.Model.View.Team.TrophyDTO;
import com.example.FYP.Api.Service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/{teamId}/header")
    public ResponseEntity<TeamHeaderDTO> getTeamHeader(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeamHeader(teamId));
    }

    @GetMapping("/{teamId}/standings")
    public ResponseEntity<List<StandingRowDTO>> getTeamStandings(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "2023") int season,
            @RequestParam(defaultValue = "39") int league // Defaults to Premier League (39) or La Liga (140)
    ) {
        // Note: league parameter is accepted but not yet used in service method
        return ResponseEntity.ok(teamService.getStandings(teamId, season));
    }

    @GetMapping("/{teamId}/trophies")
    public ResponseEntity<List<TrophyDTO>> getTeamTrophies(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTrophies(teamId));
    }
}
