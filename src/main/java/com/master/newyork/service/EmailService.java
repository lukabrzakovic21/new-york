package com.master.newyork.service;

import com.master.newyork.common.EmailBuilder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static com.master.newyork.configuration.EmailConfiguration.MAIL_SENDER;

@Service
public class EmailService {

    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendSimpleMessage(EmailBuilder email) {
        var message = new SimpleMailMessage();
        message.setFrom(MAIL_SENDER);
        message.setTo(email.getTo());
        message.setSubject(email.getSubject());
        message.setText(email.getText());
        emailSender.send(message);
    }
}
