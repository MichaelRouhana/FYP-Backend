package com.example.AzureTestProject.Api.Repository;

import com.example.AzureTestProject.Api.Messaging.WebSocket.CommunityMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityMessageRepository extends JpaRepository<CommunityMessage, Long> {
}
