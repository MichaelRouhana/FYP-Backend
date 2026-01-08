package com.example.FYP.Api.Model.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityResponseDTO {
    private Long id;
    private String logo;
    private String name;
    private String location;
    private String about;
    private List<String> rules;
    private String inviteCode;
    private List<Long> userIds;
}
