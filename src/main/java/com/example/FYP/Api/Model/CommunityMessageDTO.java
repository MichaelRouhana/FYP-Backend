package com.example.FYP.Api.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityMessageDTO {
    private String content;
    private String senderUsername;
}
