package com.example.FYP.Api.Model.Patch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityPatchDTO {

    private String logo;
    private String name;
    private String location;
    private String about;
    private List<String> rules;
}
