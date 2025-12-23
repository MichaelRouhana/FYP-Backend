package com.example.AzureTestProject.Api.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendInviteEmail(String to, String subject, String token) throws jakarta.mail.MessagingException {
        String link = "https://api.erpcloudapp.com/api/v1/organizations/invitation/accept/" + token;

        Context context = new Context();
        context.setVariable("acceptInvitationUrl", link);

        String emailContent = templateEngine.process("invitationEmail", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("charbelbouabdo4@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(emailContent, true);

        mailSender.send(message);
    }


    public void sendVerificationEmail(String to, String subject, String token) throws jakarta.mail.MessagingException {
        String link = "https://api.erpcloudapp.com/api/v1/users/verify?token=" + token;

        Context context = new Context();
        context.setVariable("acceptInvitationUrl", link);

        String emailContent = templateEngine.process("verificationEmail", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("charbelbouabdo4@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(emailContent, true);

        mailSender.send(message);
    }
}
