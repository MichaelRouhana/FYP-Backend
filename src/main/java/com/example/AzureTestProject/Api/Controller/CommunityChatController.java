package com.example.AzureTestProject.Api.Controller;

import com.example.AzureTestProject.Api.Entity.Community;
import com.example.AzureTestProject.Api.Entity.User;
import com.example.AzureTestProject.Api.Messaging.WebSocket.CommunityMessage;
import com.example.AzureTestProject.Api.Repository.CommunityMessageRepository;
import com.example.AzureTestProject.Api.Repository.CommunityRepository;
import com.example.AzureTestProject.Api.Repository.UserRepository;
import com.example.AzureTestProject.Api.Security.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class CommunityChatController {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final CommunityMessageRepository messageRepository;
    private final SecurityContext securityContext;

    @MessageMapping("/community/{communityId}/send")
    @SendTo("/topic/community/{communityId}")
    public CommunityMessage sendMessage(
            @DestinationVariable Long communityId,
            CommunityMessage message
    ) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));

        User sender = securityContext.getCurrentUser();

        if (!community.getUsers().contains(sender)) {
            throw new RuntimeException("User not a member of this community");
        }

        CommunityMessage entity = new CommunityMessage();
        entity.setCommunity(community);
        entity.setSender(sender);
        entity.setContent(message.getContent());
        entity.setSentAt(LocalDateTime.now());
        messageRepository.save(entity);

        message.setCommunity(community);
        return message;
    }
}

