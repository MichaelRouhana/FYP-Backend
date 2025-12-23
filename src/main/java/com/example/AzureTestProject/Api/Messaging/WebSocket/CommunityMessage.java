package com.example.AzureTestProject.Api.Messaging.WebSocket;

import com.example.AzureTestProject.Api.Entity.Community;
import com.example.AzureTestProject.Api.Entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CommunityMessage {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Community community;

    @ManyToOne
    private User sender;

    private String content;

    private LocalDateTime sentAt;
}
