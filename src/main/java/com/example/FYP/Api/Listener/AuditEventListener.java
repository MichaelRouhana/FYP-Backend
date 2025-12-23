package com.example.FYP.Api.Listener;

import com.example.FYP.Api.Event.AuditEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuditEventListener {

    @TransactionalEventListener
    private void handle(AuditEvent event) {

    }
}
