package com.example.FYP.Api.Model.View.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachDTO {
    private String name;
    private String photoUrl;
    private String nationality;
}

