package com.example.FYP.Api.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityMessageDTO {
    private Long id;
    private String content;
    private String senderUsername;
    private String senderEmail;
    private String sentAt;
    private Long senderId;
    
    public CommunityMessageDTO(String content, String senderUsername) {
        this.content = content;
        this.senderUsername = senderUsername;
    }
}
