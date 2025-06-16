package com.fifo.ticketing.global.service;

import com.fifo.ticketing.domain.book.dto.BookMailSendDto;
import com.fifo.ticketing.domain.like.dto.NoPayedMailDto;
import com.fifo.ticketing.domain.like.dto.ReservationStartMailDto;
import com.fifo.ticketing.global.exception.ErrorCode;
import com.fifo.ticketing.global.exception.ErrorException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String hostAddress;

    private void sendMail(String to, String subject, String templatePath, Object dto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(hostAddress);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("mailDto", dto);

            String htmlContent = templateEngine.process(templatePath, context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new ErrorException(ErrorCode.FAIL_EMAIL_SEND);
        }
    }

    public void sendPerformanceCancelNoticeMail(BookMailSendDto dto) {
        sendMail(dto.getEmailAddr(), dto.getPerformanceTitle() + "의 공연이 취소되었습니다.",
            "book/complete_mail", dto);
    }

    public void sendBookInformationNoticeMail(BookMailSendDto dto) {
        try {
            sendMail(dto.getEmailAddr(), dto.getTitle(), "book/complete_mail", dto);
        } catch (ErrorException e) {
            throw new RuntimeException("메일 전송 실패", e);
        }
    }

    @Async("mailExecutor")
    public void sendReservationStartNoticeMail(ReservationStartMailDto dto) {
        sendMail(dto.getEmail(), "[알림] " + dto.getPerformanceTitle() + " 예매 시작 30분 전입니다.",
            "mail/reservation_start_notice", dto);
    }

    @Async("mailExecutor")
    public void sendNoPayedNoticeMail(NoPayedMailDto dto) {
        sendMail(dto.getEmail(), "[미예매 알림] " + dto.getPerformanceTitle() + " 예매를 잊으셨나요?",
            "mail/no_payed_notice", dto);
    }
}
