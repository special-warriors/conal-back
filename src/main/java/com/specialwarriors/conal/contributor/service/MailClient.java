package com.specialwarriors.conal.contributor.service;


import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.contributor.exception.ContributorException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailClient {

    private final JavaMailSender mailSender;

    public void send(String to, String subject, String text) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(text, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new GeneralException(ContributorException.MAIL_ACCESS_ERROR);
        }
    }

    @Async
    public void sendAsync(String to, String subject, String body) {
        send(to, subject, body);
    }
}
