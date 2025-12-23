package com.example.FYP.Api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "community", cascade = {CascadeType.ALL, CascadeType.REMOVE}, orphanRemoval = true)
    @JsonIgnore
    private Set<CommunityRole> roles = new HashSet<>();
}
