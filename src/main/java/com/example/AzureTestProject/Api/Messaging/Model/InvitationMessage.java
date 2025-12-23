package com.example.AzureTestProject.Api.Messaging.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvitationMessage implements Serializable {
    private String email;
    private String token;
}
