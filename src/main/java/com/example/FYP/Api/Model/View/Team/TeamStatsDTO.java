package com.example.FYP.Api.Model.View.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatsDTO {
    private Summary summary;
    private Attacking attacking;
    private Passing passing;
    private Defending defending;
    private Other other;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer played;
        private Integer wins;
        private Integer draws;
        private Integer loses;
        private String form; // e.g., "WWDLW"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attacking {
        private Integer goalsScored;
        private Integer penaltiesScored;
        private Integer penaltiesMissed;
        private Integer shotsOnGoal;
        private Integer shotsOffGoal;
        private Integer totalShots;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Passing {
        private Integer totalPasses;
        private Integer passesAccurate;
        private Double passAccuracyPercentage; // 0-100
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Defending {
        private Integer goalsConceded;
        private Integer cleanSheets;
        private Integer saves;
        private Integer tackles;
        private Integer interceptions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Other {
        private Integer yellowCards;
        private Integer redCards;
        private Integer fouls;
        private Integer corners;
        private Integer offsides;
    }
}
