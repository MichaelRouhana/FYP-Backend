package com.example.FYP.Api.Model.View;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunityViewDTO {
    private Long id;
    private String logo;
    private String name;
    private String location;
    private String about;
    private List<String> rules;
    private List<Long> userIds;
}
