package com.example.AzureTestProject.Api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "invitation")
@Data
@NoArgsConstructor
public class Invitation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "uuid", updatable = false, nullable = false, unique = true)
    private String uuid;

    @NotBlank(message = "Organization UUID cannot be blank")
    @Column(name = "organization_uuid", nullable = false, unique = false)
    private String organizationUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User invitee;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }


}
