package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.global.service.RedisService
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jakarta.mail.Message
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import jakarta.servlet.http.HttpSession
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mail.javamail.JavaMailSender
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine

@ExtendWith(MockKExtension::class)
class EmailAuthServiceTests {
    private val TEST_EMAIL_FORM = "test@example.com"

    @MockK
    private lateinit var redisService: RedisService

    @MockK
    private lateinit var mailSender: JavaMailSender

    @MockK
    private lateinit var templateEngine: SpringTemplateEngine

    @MockK
    private lateinit var mimeMessage: MimeMessage

    @MockK
    private lateinit var session: HttpSession

    lateinit var emailAuthService: EmailAuthService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { mailSender.createMimeMessage() } returns mimeMessage
        every {
            templateEngine.process(eq("mail"), match { it is Context })
        } returns "<html><body>Code: 123456</body></html>"

        every { mimeMessage.setSubject(any()) } just Runs
        every { mimeMessage.setText(any()) } just Runs
        every { mimeMessage.setText(any(), any(), any()) } just Runs
        every { mimeMessage.setContent(any(), any()) } just Runs
        every { mimeMessage.setFrom(any<String>()) } just Runs
        every { mimeMessage.addRecipients(eq(Message.RecipientType.TO), any<String>()) } just Runs

        emailAuthService = EmailAuthService(
            redisService,
            mailSender,
            templateEngine,
            TEST_EMAIL_FORM
        )
    }

    @Test
    @Throws(MessagingException::class)
    fun testSendEmail() {
        val toMail = "test@test.com"

        every {
            redisService.setValuesWithTimeout(
                "EAC:$toMail", any(), 5 * 60 * 1000L
            )
        } just Runs

        every { mailSender.send(mimeMessage) } just Runs

        emailAuthService.sendEmail(toMail)

        verify(exactly = 1) {
            redisService.setValuesWithTimeout("EAC:$toMail", any(), 5 * 60 * 1000L)
        }

        verify(exactly = 1) { mailSender.send(mimeMessage) }
    }

    @Test
    fun testCorrectAuthCode() {
        val email = "test@test.com"
        val authCode = "123456"
        every { redisService.getValues("EAC:$email") } returns authCode
        every { redisService.deleteValues("EAC:$email") } just Runs
        every { session.setAttribute("emailVerified", email) } just Runs

        val checked = emailAuthService.checkAuthCode(email, authCode, session)

        assertThat(checked).isTrue()
        verify { redisService.deleteValues("EAC:$email") }
        verify { session.setAttribute("emailVerified", email) }
    }

    @Test
    fun testIncorrectAuthCode() {
        val email = "test@test.com"
        val authCode = "123456"
        every { redisService.getValues("EAC:$email") } returns "654321"

        val checked = emailAuthService.checkAuthCode(email, authCode, session)

        assertThat(checked).isFalse()
        verify(exactly = 0) { redisService.deleteValues(any()) }
        verify(exactly = 0) { session.setAttribute(any(), any()) }
    }
}