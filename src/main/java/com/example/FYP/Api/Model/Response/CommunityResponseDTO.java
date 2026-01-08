package com.example.FYP.Api.Model.Response;

import com.example.FYP.Api.Model.View.UserViewDTO;
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
    private List<Long> moderatorIds; // Optional: List of moderator user IDs
    private List<UserViewDTO> leaderboard; // Top 3 members by points
    private List<UserViewDTO> moderators;  // Admins and Moderators
}
