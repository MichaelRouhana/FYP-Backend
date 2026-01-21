package com.example.FYP.Api.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${backend.domain}")
    private String domain;

    @Value("${spring.mail.username}")
    private String username;


    public void sendVerificationEmail(String to, String subject, String token) throws jakarta.mail.MessagingException {
        String link = domain + "/api/v1/users/verify?token=" + token;

        Context context = new Context();
        context.setVariable("acceptInvitationUrl", link);

        String emailContent = templateEngine.process("verificationEmail", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(username);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(emailContent, true);

        mailSender.send(message);
    }
}
