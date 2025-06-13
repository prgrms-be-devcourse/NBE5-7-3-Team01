package com.fifo.ticketing.domain.like.service

import com.fifo.ticketing.domain.like.dto.NoPayedMailDto
import com.fifo.ticketing.domain.like.dto.ReservationStartMailDto
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import jakarta.mail.MessagingException
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine

@Service
class LikeMailService(
    private val javaMailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine,
    @Value("\${spring.mail.username}")
    private val fromAddress: String
) {

    @Async("mailExecutor")
    fun reservationStart(dto: ReservationStartMailDto) {
        try {
            val message = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(dto.email)
            helper.setFrom(fromAddress)
            helper.setSubject("[알림] " + dto.performanceTitle + " 예매 시작 30분 전입니다.")

            val context = Context()
            context.setVariable("mailDto", dto)

            val htmlContent = templateEngine.process("mail/reservation_start_notice", context)
            helper.setText(htmlContent, true)

            javaMailSender.send(message)
        } catch (e: MessagingException) {
            throw ErrorException(ErrorCode.FAIL_EMAIL_SEND)
        }
    }

    @Async("mailExecutor")
    fun noPayedPerformance(dto: NoPayedMailDto) {
        try {
            val message = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(dto.email)
            helper.setFrom(fromAddress)
            helper.setSubject("[미예매 알림] " + dto.performanceTitle + " 예매를 잊으셨나요?")

            val context = Context()
            context.setVariable("mailDto", dto)

            val htmlContent = templateEngine.process("mail/no_payed_notice", context)
            helper.setText(htmlContent, true)

            javaMailSender.send(message)
        } catch (e: MessagingException) {
            throw ErrorException(ErrorCode.FAIL_EMAIL_SEND)
        }
    }
}