package com.fifo.ticketing.global.service

import com.fifo.ticketing.domain.book.dto.BookMailSendDto
import com.fifo.ticketing.domain.like.dto.NoPayedMailDto
import com.fifo.ticketing.domain.like.dto.ReservationStartMailDto
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class MailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,

    @Value("\${spring.mail.username}")
    private val hostAddress: String
) {
    private fun sendMail(to: String, subject: String, templatePath: String, dto: Any) {
        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(to)
            helper.setFrom(hostAddress)
            helper.setSubject(subject)

            val context = Context().apply {
                setVariable("mailDto", dto)
            }

            val htmlContent = templateEngine.process(templatePath, context)
            helper.setText(htmlContent, true)

            mailSender.send(message)
        } catch (e: MessagingException) {
            throw ErrorException(ErrorCode.FAIL_EMAIL_SEND)
        }
    }

    fun sendPerformanceCancelNoticeMail(dto: BookMailSendDto) {
        sendMail(
            dto.emailAddr,
            "${dto.performanceTitle}의 공연이 취소되었습니다.",
            "book/complete_mail",
            dto
        )
    }

    fun sendBookInformationNoticeMail(dto: BookMailSendDto) {
        try {
            sendMail(dto.emailAddr, dto.title, "book/complete_mail", dto)
        } catch (e: ErrorException) {
            throw RuntimeException("메일 전송 실패", e)
        }
    }

    @Async("mailExecutor")
    fun sendReservationStartNoticeMail(dto: ReservationStartMailDto) {
        sendMail(
            dto.email,
            "[알림] ${dto.performanceTitle} 예매 시작 30분 전입니다.",
            "mail/reservation_start_notice",
            dto
        )
    }

    @Async("mailExecutor")
    fun sendNoPayedNoticeMail(dto: NoPayedMailDto) {
        sendMail(
            dto.email,
            "[미예매 알림] ${dto.performanceTitle} 예매를 잊으셨나요?",
            "mail/no_payed_notice",
            dto
        )
    }
}