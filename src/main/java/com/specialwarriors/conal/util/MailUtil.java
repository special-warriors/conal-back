package com.specialwarriors.conal.util;

import com.specialwarriors.conal.vote.dto.response.VoteFormResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
public class MailUtil {

    @Value("${BASE_URL}")
    private String baseUrl;

    @Value("${MAIL_ACCOUNT}")
    private String serviceMail;

    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    public void sendVoteForm(VoteFormResponse response) {
        HashMap<String, Object> templateModel = new HashMap<>();
        templateModel.put("repoId", response.repoId());
        templateModel.put("userToken", response.userToken());
        templateModel.put("emails", response.voteTargetEmails());

        String voteCompleteUrl = baseUrl + "/repositories/%d/votes"
                .formatted(response.repoId());
        templateModel.put("voteCompleteUrl", voteCompleteUrl);

        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);

        String htmlBody = templateEngine.process("vote-form.html", thymeleafContext);

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(serviceMail);
            helper.setTo(response.email());
            helper.setSubject("[Conal] 주간 투표 참여 안내");
            helper.setText(htmlBody, true);

            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new IllegalStateException("메일 발송 중 예외 발생", e);
        }
    }
}
