package com.master.newyork.service;

import com.master.newyork.common.EmailBuilder;
import com.master.newyork.common.event.ItemAvailableAgain;
import com.master.newyork.common.event.ItemNoLongerAvailable;
import com.master.newyork.common.event.RegistrationRequestStatusChanged;
import com.master.newyork.common.event.UserStatusChanged;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.master.newyork.configuration.EmailConfiguration.CUSTOMER_GROUP_EMAIL;
import static com.master.newyork.configuration.RabbitMqConfiguration.ITEM_AVAILABLE_AGAIN;
import static com.master.newyork.configuration.RabbitMqConfiguration.ITEM_NO_LONGER_AVAILABLE;
import static com.master.newyork.configuration.RabbitMqConfiguration.REGISTRATION_REQUEST_STATUS_CHANGED;
import static com.master.newyork.configuration.RabbitMqConfiguration.USER_STATUS_CHANGED;

@Component
public class RabbitMqService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqService.class);
    private final RabbitTemplate rabbitTemplate;
    private final EmailService emailService;

    public RabbitMqService(RabbitTemplate rabbitTemplate, EmailService emailService) {
        this.rabbitTemplate = rabbitTemplate;
        this.emailService = emailService;
    }

    @RabbitListener(queues = REGISTRATION_REQUEST_STATUS_CHANGED, messageConverter = "jsonMessageConverter")
    public void consumeRegistrationRequestStatusChanged(RegistrationRequestStatusChanged registrationRequestStatusChanged) throws MessagingException {

        logger.info("Consumed message from queue {} with body {}", REGISTRATION_REQUEST_STATUS_CHANGED, registrationRequestStatusChanged);
        var text = new StringBuilder();
        text.append("Dear ");
        text.append(registrationRequestStatusChanged.getFirstname() + " " + registrationRequestStatusChanged.getLastname() + ",");
        text.append(" your registration request has been " + registrationRequestStatusChanged.getStatus() + ".");
        var subject = "Registration request " + registrationRequestStatusChanged.getStatus();
        var email = EmailBuilder.builder()
                .subject(subject)
                .text(emailService.composeEmail(text.toString()))
                .to(registrationRequestStatusChanged.getEmail())
                .build();
        emailService.sendSimpleMessage(email);
        logger.info("Email sent to {}", registrationRequestStatusChanged.getEmail());
    }

    @RabbitListener(queues = USER_STATUS_CHANGED, messageConverter = "jsonMessageConverter")
    public void consumeUserStatusChanged(UserStatusChanged userStatusChanged) throws MessagingException {

        logger.info("Consumed message from queue {} with body {}", USER_STATUS_CHANGED, userStatusChanged);

        var text = new StringBuilder();
        text.append("Dear ");
        text.append(userStatusChanged.getFirstname() + " " + userStatusChanged.getLastname() + ",");
        text.append(" your account has been " + userStatusChanged.getStatus() + ".");
        var subject = "User status changed to  " + userStatusChanged.getStatus();
        var email = EmailBuilder.builder()
                .subject(subject)
                .text(emailService.composeEmail(text.toString()))
                .to(userStatusChanged.getEmail())
                .build();
        emailService.sendSimpleMessage(email);
        logger.info("Email sent to {}", userStatusChanged.getEmail());

    }

    @RabbitListener(queues = ITEM_AVAILABLE_AGAIN, messageConverter = "jsonMessageConverter")
    public void consumeItemAvailableAgain(ItemAvailableAgain item) {

        logger.info("Consumed message from queue {} with body {}", ITEM_AVAILABLE_AGAIN, item);
        var text = new StringBuilder();
        text.append("Item ");
        text.append(item.getItem() + " that you have requested is available again. Go to our site and try to buy this item.");
        var subject = " Item  " + item.getItem() + " available again";

        item.getEmails().forEach(email -> {
            var emailBuilder = EmailBuilder.builder()
                    .subject(subject)
                    .text(emailService.composeEmail(text.toString()))
                    .to(email)
                    .build();
            try {
                emailService.sendSimpleMessage(emailBuilder);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
        logger.info("Email sent to {}", item.getEmails());

    }

    @RabbitListener(queues = ITEM_NO_LONGER_AVAILABLE, messageConverter = "jsonMessageConverter")
    public void consumeItemNoLongerAvailable(ItemNoLongerAvailable item) throws MessagingException {

        logger.info("Consumed message from queue {} with body {}", ITEM_NO_LONGER_AVAILABLE, item);
        var text = new StringBuilder();
        text.append("Item ");
        text.append(item.getItem() + " is no longer available.");
        var subject = " Item  " + item.getItem() + " no longer available.";
        var email = EmailBuilder.builder()
                .subject(subject)
                .text(emailService.composeEmail(text.toString()))
                .to(CUSTOMER_GROUP_EMAIL)
                .build();
        emailService.sendSimpleMessage(email);
        logger.info("Email sent to {}", CUSTOMER_GROUP_EMAIL);

    }
}
