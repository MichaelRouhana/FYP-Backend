package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Messaging.WebSocket.CommunityMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityMessageRepository extends JpaRepository<CommunityMessage, Long> {
    // Add this method to fetch messages ordered by time
    List<CommunityMessage> findByCommunityIdOrderBySentAtAsc(Long communityId);
}
