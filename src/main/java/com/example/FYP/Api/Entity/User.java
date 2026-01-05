package com.example.FYP.Api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),

        })
public class User extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    @JsonIgnore
    private long id;


    @Column(name = "username")
    @NotBlank
    private String username;

    private Long points;


    @OneToMany(mappedBy = "user")
    private List<Bet> bets;


    @Column(name = "email", unique = true)
    @Email
    @NotBlank
    private String email;

    @JsonIgnore
    @NotBlank
    private String password;

    @Column(name = "is_verified")
    @JsonIgnore

    private boolean isVerified;

    private boolean isLocked;


    @Column(name = "pfp")
    @NotBlank
    private String pfp;

    @Column(name = "about", length = 1000)
    private String about; // User's bio/about section

    @Column(name = "country", length = 100)
    private String country; // User's country from signup

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @JsonIgnore
    private Set<UserRole> roles = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "community_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "org_role_id")
    )
    @JsonIgnore
    private Set<CommunityRole> communityRoles = new HashSet<>();


    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Community> communities;



    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private VerificationToken verificationToken;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User userInfo)) return false;
        return id == userInfo.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }


}