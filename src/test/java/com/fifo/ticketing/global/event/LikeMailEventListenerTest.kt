import com.fifo.ticketing.TicketingApplication
import com.fifo.ticketing.domain.like.dto.NoPayedMailDto
import com.fifo.ticketing.domain.like.dto.ReservationStartMailDto
import com.fifo.ticketing.global.service.MailService
import org.awaitility.Awaitility.await
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@SpringBootTest(classes = [TicketingApplication::class])
@ActiveProfiles("ci")
class LikeMailEventListenerIntegrationTest {

    @Autowired
    private lateinit var eventPublisher: ApplicationEventPublisher

    @MockitoBean
    private lateinit var mailService: MailService

    @Test
    fun `예약 시작 알림 이벤트가 발행되면 리스너가 메일 전송을 호출한다`() {
        val dto = ReservationStartMailDto(
            username = "테스트유저",
            email = "test@fifo.com",
            performanceTitle = "공연 제목",
            reservationStartTime = LocalDateTime.of(2025, 10, 1, 19, 0)
        )

        eventPublisher.publishEvent(dto)

        await().atMost(2, TimeUnit.SECONDS).untilAsserted {
            verify(mailService).sendReservationStartNoticeMail(dto)
        }
    }

    @Test
    fun `미결제 이벤트가 발행되면 리스너가 메일 전송을 호출한다`() {
        val dto = NoPayedMailDto(
            username = "테스트유저",
            email = "test@fifo.com",
            performanceTitle = "공연 제목",
            availableSeats = 10
        )

        eventPublisher.publishEvent(dto)

        await().atMost(2, TimeUnit.SECONDS).untilAsserted {
            verify(mailService).sendNoPayedNoticeMail(dto)
        }
    }
}
