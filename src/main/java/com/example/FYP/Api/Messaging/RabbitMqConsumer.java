package com.example.FYP.Api.Messaging;

import com.example.FYP.Api.Messaging.Model.EmailVerificationMessage;
import com.example.FYP.Api.Messaging.Model.InvitationMessage;
import com.example.FYP.Api.Service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqConsumer {

    private final MailService mailService;

    @RabbitListener(queues = "inviteQueue")
    public void receiveInvite(InvitationMessage message) {

        log.info("Sending Invite to : " + message.getEmail());

        try {
            mailService.sendInviteEmail(message.getEmail(), "You're Invited!", message.getToken());
        } catch (jakarta.mail.MessagingException e) {
            e.printStackTrace();
        }
        log.info("Invitation email sent successfully");
    }


    @RabbitListener(queues = "verificationQueue")
    public void receiveVerification(EmailVerificationMessage message) {
        log.info("Sending Invite to : " + message.getEmail());

        try {
            mailService.sendVerificationEmail(message.getEmail(), "Verification Email", message.getToken());
        } catch (jakarta.mail.MessagingException e) {
            e.printStackTrace();
        }
        log.info("Invitation email sent successfully");

    }


}
