package com.example.FYP.Api.Model.View.Player;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDetailedStatsDTO {
    private Summary summary;
    private Attacking attacking;
    private Passing passing;
    private Defending defending;
    private Discipline discipline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer matchesPlayed;
        private Integer minutesPlayed;
        private Integer goals;
        private Integer assists;
        private String rating; // Can be a decimal string like "7.5"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attacking {
        private Integer shotsTotal;
        private Integer shotsOnTarget;
        private Integer dribblesAttempted;
        private Integer dribblesSuccess;
        private Integer penaltiesScored;
        private Integer penaltiesMissed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Passing {
        private Integer totalPasses;
        private Integer keyPasses;
        private Integer passAccuracy; // Percentage as integer (0-100)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Defending {
        private Integer tacklesTotal;
        private Integer interceptions;
        private Integer blocks;
        private Integer duelsTotal;
        private Integer duelsWon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Discipline {
        private Integer yellowCards;
        private Integer redCards;
        private Integer foulsCommitted;
        private Integer foulsDrawn;
    }
}

