package com.example.FYP.Api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nimbusds.openid.connect.sdk.assurance.evidences.Organization;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {
    @Id
    @GeneratedValue
    private Long id;

    private String username;
    private String action;
    private String path;
    private String httpMethod;
    private String ip;
    private LocalDateTime timestamp;


}
