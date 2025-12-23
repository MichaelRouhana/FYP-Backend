package com.example.AzureTestProject.Api.Entity;

import com.example.AzureTestProject.Api.Model.Constant.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ROLES")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    @JsonIgnore
    private long id;

    @NotNull(message = "role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

}