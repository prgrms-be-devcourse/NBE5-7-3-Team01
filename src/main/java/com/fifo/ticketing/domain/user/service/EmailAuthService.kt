package com.fifo.ticketing.domain.user.service

import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import jakarta.servlet.http.HttpSession
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.security.SecureRandom

@Service
@RequiredArgsConstructor
class EmailAuthService {
    private val redisService: RedisService? = null
    private val emailSender: JavaMailSender? = null
    private val templateEngine: SpringTemplateEngine? = null

    @Value("\${spring.mail.username}")
    private val setForm: String? = null

    @Throws(MessagingException::class)
    fun sendEmail(toEmail: String) {
        val authCode = createCode()
        val setKey = "EAC:$toEmail"
        redisService!!.setValuesWithTimeout(setKey, authCode, (5 * 60 * 1000).toLong())
        val emailForm = createEmailForm(toEmail, authCode)
        emailSender!!.send(emailForm)
    }

    fun createCode(): String {
        val random = SecureRandom()
        val authCode = StringBuilder()

        for (i in 0..5) {
            authCode.append(random.nextInt(9))
        }

        return authCode.toString()
    }

    @Throws(MessagingException::class)
    fun createEmailForm(email: String?, authCode: String?): MimeMessage {
        val title = "Ticketing 회원가입 인증 번호"
        val message = emailSender!!.createMimeMessage()
        message.addRecipients(MimeMessage.RecipientType.TO, email)
        message.subject = title
        message.setFrom(setForm)
        message.setText(setContext(authCode), "utf-8", "html")
        return message
    }

    fun setContext(authCode: String?): String {
        val context = Context()
        context.setVariable("code", authCode)
        return templateEngine!!.process("mail", context)
    }

    fun checkAuthCode(toEmail: String, authCode: String, session: HttpSession): Boolean {
        val key = "EAC:$toEmail"
        val findAuthCode = redisService!!.getValues(key) ?: return false

        if (findAuthCode == authCode) {
            redisService.deleteValues(key)
            session.setAttribute("emailVerified", toEmail)
            return true
        }
        return false
    }
}
