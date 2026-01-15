package com.example.FYP.Api.Model.View.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SquadMemberDTO {
    private Long id;
    private String name;
    private String photoUrl;
    private String position; // GK, DEF, MID, FWD
    private Integer age;
    private Integer height; // in cm
    private String marketValue;
    private LocalDate contractUntil;
    private String previousClub;
}

