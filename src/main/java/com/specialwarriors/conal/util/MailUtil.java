package com.specialwarriors.conal.util;

import com.specialwarriors.conal.contribution.dto.response.ContributionFormResponse;
import com.specialwarriors.conal.vote.dto.response.VoteFormResponse;
import com.specialwarriors.conal.vote.dto.response.VoteResultResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

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
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("repoId", response.repoId());
        templateModel.put("userToken", response.userToken());
        templateModel.put("emails", response.voteTargetEmails());

        String voteCompleteUrl = baseUrl + "/repositories/%d/votes".formatted(response.repoId());
        templateModel.put("voteCompleteUrl", voteCompleteUrl);

        sendHtmlMail(response.email(),
                "[Conal] 주간 투표 참여 안내",
                "vote/form.html",
                templateModel
        );
    }

    public void sendVoteResult(String to, VoteResultResponse response) {
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("response", response);

        sendHtmlMail(to,
                "[Conal] 주간 투표 결과 안내",
                "vote/result.html",
                templateModel
        );
    }

    private void sendHtmlMail(String to, String subject, String templateName,
            Map<String, Object> templateModel) {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);

        String htmlBody = templateEngine.process(templateName, thymeleafContext);
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(serviceMail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new IllegalStateException("메일 발송 중 예외 발생", e);
        }
    }

    public void sendContributionForm(ContributionFormResponse response) {
        HashMap<String, Object> templateModel = new HashMap<>();
        templateModel.put("email", response.email());

        String contributionAnalysisCompleteUrl = baseUrl + "/users/%d/repositories/%d"
                .formatted(response.userId(), response.repoId());
        templateModel.put("contributionAnalysisCompleteUrl", contributionAnalysisCompleteUrl);

        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);

        String htmlBody = templateEngine.process("contribution-form.html", thymeleafContext);

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(serviceMail);
            helper.setTo(response.email());
            helper.setSubject("[Conal] 기여도 분석 결과 알림");
            helper.setText(htmlBody, true);

            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new IllegalStateException("메일 발송 중 예외 발생", e);
        }
    }
}
