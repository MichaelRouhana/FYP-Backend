package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Messaging.WebSocket.CommunityMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityMessageRepository extends JpaRepository<CommunityMessage, Long> {
    List<CommunityMessage> findByCommunityIdOrderBySentAtAsc(Long communityId);
}
