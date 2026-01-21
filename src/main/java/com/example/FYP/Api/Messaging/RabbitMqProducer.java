package com.example.FYP.Api.Messaging;

import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Messaging.Model.EmailVerificationMessage;
import com.example.FYP.Api.Messaging.Model.InvitationMessage;
import com.example.FYP.Api.Service.MailService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqProducer {

    private final RabbitTemplate rabbitTemplate;
    private final MailService mailService;

    @CircuitBreaker(name = "rabbitmqService", fallbackMethod = "verificationFallBack")
    public void sendVerification(String queueName, EmailVerificationMessage message) {
        rabbitTemplate.convertAndSend(queueName, message);
    }

    public void verificationFallBack(String queueName, EmailVerificationMessage message, Throwable t) {
        try {
            mailService.sendVerificationEmail(message.getEmail(), "Verification", message.getToken());
        } catch (MessagingException e) {
            throw new ApiRequestException("RabbitMQ is down, retry later");
        }
    }


}
