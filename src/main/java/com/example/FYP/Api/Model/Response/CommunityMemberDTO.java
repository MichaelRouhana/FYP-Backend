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
public class CommunityMemberDTO {
    private Long id;
    private String username;
    private String email;
    private String pfp;
    private Long points;
    private String about;
    private String country;
    private List<String> roles; // Community roles: OWNER, MODERATOR, MEMBER
}

