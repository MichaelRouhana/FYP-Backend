package com.example.AzureTestProject.Api.Entity;

import com.example.AzureTestProject.Api.Model.Constant.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "org_project")
@Data
@NoArgsConstructor
public class Project extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "uuid", updatable = false, nullable = false, unique = true)
    private String uuid;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Type cannot be blank")
    @Size(min = 3, max = 50, message = "Type must be between 3 and 50 characters")
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull(message = "Project status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "project_status", nullable = false)
    private ProjectStatus projectStatus;



    @Column(name = "attachment")
    private String attachment;

    @Size(min = 10, max = 200, message = "Description must be between 10 and 200 characters")
    @Column(name = "description")
    private String description;

    @Min(value = 0, message = "Progress must be at least 0")
    @Max(value = 100, message = "Progress must not exceed 100")
    private int progress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Organization organization;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_userinfo",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    @ToString.Exclude
    private Set<User> users = new HashSet<>();


    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        progress = 0;
    }
}
