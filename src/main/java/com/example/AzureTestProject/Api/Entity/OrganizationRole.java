package com.example.AzureTestProject.Api.Entity;

import com.example.AzureTestProject.Api.Model.Constant.OrganizationRoles;
import com.example.AzureTestProject.Api.Model.Constant.Tier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "organization_roles")
public class OrganizationRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private OrganizationRoles role;


    @Enumerated(EnumType.STRING)
    private Tier tier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @ManyToMany(mappedBy = "organizationRoles", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        tier = Tier.ONE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganizationRole other)) return false;
        return role == other.role && organization != null && organization.equals(other.organization);
    }

    @Override
    public int hashCode() {
        int result = role != null ? role.hashCode() : 0;
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        return result;
    }

}
