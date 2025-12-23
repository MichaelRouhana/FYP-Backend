package com.example.AzureTestProject.Api.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "communities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Community extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String logo;

    @Column(nullable = false, unique = true)
    private String name;

    private String location;

    @Column(length = 1000)
    private String about;

    @ElementCollection
    @CollectionTable(
            name = "community_rules",
            joinColumns = @JoinColumn(name = "community_id")
    )
    @Column(name = "rule")
    private List<String> rules;


    @ManyToMany
    @JoinTable(
            name = "community_users",
            joinColumns = @JoinColumn(name = "community_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;
}
