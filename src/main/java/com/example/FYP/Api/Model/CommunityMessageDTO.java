package com.example.FYP.Api.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityMessageDTO {
    private Long id; // For REST API responses (message history)
    private String content;
    private String senderUsername;
    private String senderEmail; // Email for identity verification
    private String sentAt; // ISO date string for frontend
    private Long senderId; // Optional, for frontend reference
    
    // Constructor for WebSocket (backward compatibility)
    public CommunityMessageDTO(String content, String senderUsername) {
        this.content = content;
        this.senderUsername = senderUsername;
    }
}
