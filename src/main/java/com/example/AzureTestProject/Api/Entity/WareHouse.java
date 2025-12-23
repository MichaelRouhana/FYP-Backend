package com.example.AzureTestProject.Api.Entity;

import com.example.AzureTestProject.Api.Util.CodeGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "warehouse")
public class WareHouse extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", updatable = false, nullable = false, unique = true)
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "defaultWareHouse")
    private Boolean defaultWareHouse;

    @NotBlank(message = "city cannot be blank")
    @Column(name = "city")
    private String city;

    @NotBlank(message = "country cannot be blank")
    @Column(name = "country")
    private String country;

    @NotBlank(message = "street cannot be blank")
    @Column(name = "street")
    private String street;

    @Column(name = "pinCode")
    private String pinCode;

    @Size(max = 200, message = "Details must not exceed 500 characters")
    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @JsonIgnore
    @ToString.Exclude
    private Organization organization;




    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = CodeGenerator.generateCode("WH");

        }
        if (defaultWareHouse == null) {
            defaultWareHouse = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WareHouse warehouse)) return false;
        return id == warehouse.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
