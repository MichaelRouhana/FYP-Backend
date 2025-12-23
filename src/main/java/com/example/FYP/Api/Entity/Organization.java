package com.example.FYP.Api.Entity;

import com.example.FYP.Api.Model.Constant.BuisnessType;
import com.example.FYP.Api.Model.Constant.OrganizationType;
import com.example.FYP.Api.Validation.Annotation.ValidTRN;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "org_organization")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class Organization extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    @JsonIgnore
    private long id;

    @Column(name = "uuid", updatable = false, nullable = false, unique = true)
    private String uuid;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User owner;

    private String icon;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 8, max = 100, message = "Name must be between 8 and 100 characters")
    @Column(length = 100, nullable = false)
    private String name;

    @NotBlank(message = "Phone number cannot be blank")
    @Size(min = 8, max = 16, message = "Phone number must be between 8 and 16 characters")
    @Column(name = "phone_number", length = 16, nullable = false)
    private String phoneNumber;


    @NotBlank(message = "TRN cannot be blank")
    @Column(length = 16, nullable = false)
    @ValidTRN
    private String trn;

    @NotNull(message = "Business type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false)
    private BuisnessType businessType;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Column(nullable = false)
    private String email;

    @NotNull(message = "Organization type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "organization_type", nullable = false)
    private OrganizationType organizationType;

    @NotBlank(message = "license cannot be blank")
    @Size(min = 8, max = 16, message = "license must be between 8 and 16 characters")
    @Column(length = 16, nullable = false)
    private String license;

    @NotBlank(message = "country cannot be blank")
    @Column(name = "country", nullable = false)
    private String country;

    @NotBlank(message = "currency cannot be blank")
    @Column(name = "currency", nullable = false)
    private String currency;

    @Future(message = "Expiry date must be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Past(message = "Establishment date must be in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "establishment_date", nullable = false)
    private LocalDate establishmentDate;


    @Size(min = 8, max = 200, message = "Description must be between 8 and 200 characters")
    @Column(nullable = true, name = "description")
    private String description;

    @NotNull(message = "vat cannot be null")
    @Column(nullable = false)
    private BigDecimal vat;

    @NotNull(message = "shares cannot be null")
    @Column(nullable = false)
    private BigDecimal shares;


    @OneToOne(mappedBy = "organization")
    private Subscription subscription;




    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_organization",
            joinColumns = @JoinColumn(name = "organization_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<User> users;



    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Organization)) return false;
        Organization org = (Organization) o;
        return Objects.equals(this.id, org.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
