package com.example.FYP.Api.Entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
//@EntityListeners({AuditingEntityListener.class, AuditListener.class})
@EntityListeners({AuditingEntityListener.class})
public abstract class AuditableEntity extends BaseEntity {

}
