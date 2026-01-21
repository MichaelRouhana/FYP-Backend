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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Defending {
        private Integer goalsConceded;
    private Integer cleanSheets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Other {
    private Integer yellowCards;
    private Integer redCards;
}
}
