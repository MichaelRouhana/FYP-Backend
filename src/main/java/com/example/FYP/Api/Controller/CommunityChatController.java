package com.example.FYP.Api.Controller;

import com.example.FYP.Api.Entity.Community;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Messaging.WebSocket.CommunityMessage;
import com.example.FYP.Api.Model.CommunityMessageDTO;
import com.example.FYP.Api.Repository.CommunityMessageRepository;
import com.example.FYP.Api.Repository.CommunityRepository;
import com.example.FYP.Api.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class CommunityChatController {

    private final CommunityRepository communityRepository;
    private final CommunityMessageRepository messageRepository;
    private final UserRepository userRepository;


    @Transactional
    @MessageMapping("/community/{communityId}/send")
    @SendTo("/topic/community/{communityId}")
    public CommunityMessageDTO sendMessage(
            @DestinationVariable Long communityId,
            @Payload CommunityMessageDTO payload,
            Authentication authentication   // ✅ Inject authenticated user
    ) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));

        // Get authenticated user from principal
        String email = authentication.getName(); // username/email
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!community.getUsers().contains(sender)) {
            throw ApiRequestException.badRequest("User not a member of this community");
        }

        // Save message
        CommunityMessage entity = new CommunityMessage();
        entity.setCommunity(community);
        entity.setSender(sender);
        entity.setContent(payload.getContent());
        LocalDateTime now = LocalDateTime.now();
        entity.setSentAt(now);
        CommunityMessage saved = messageRepository.save(entity);

        // Return DTO with full details for WebSocket broadcast
        CommunityMessageDTO dto = new CommunityMessageDTO();
        dto.setId(saved.getId());
        dto.setContent(saved.getContent());
        dto.setSenderUsername(sender.getUsername());
        dto.setSenderId(sender.getId());
        dto.setSentAt(now.format(java.time.format.DateTimeFormatter.ISO_DATE_TIME));
        return dto;
    }
}
