package com.example.AzureTestProject.Api.Listener;

import com.example.AzureTestProject.Api.Event.AuditEvent;
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
