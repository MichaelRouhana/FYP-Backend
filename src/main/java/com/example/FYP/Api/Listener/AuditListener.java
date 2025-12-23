package com.example.FYP.Api.Listener;

import com.example.FYP.Api.Event.AuditEvent;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditListener {

    private final ApplicationEventPublisher publisher;

    @PrePersist
    public void prePersist(Object entity) {
        publisher.publishEvent(new AuditEvent(entity, "CREATE", entity));

    }

    @PreUpdate
    public void preUpdate(Object entity) {
        publisher.publishEvent(new AuditEvent(entity, "UPDATE", entity));
    }

    @PreRemove
    public void preRemove(Object entity) {
        publisher.publishEvent(new AuditEvent(entity, "DELETE", entity));

    }

}
