package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.domain.like.repository.LikeRepository
import com.fifo.ticketing.domain.performance.dto.LikedPerformanceDto
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.entity.Place
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.global.entity.File
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("ci")
@ExtendWith(MockKExtension::class)
internal class MyPageServiceTests {
    private val UPLOAD = "/tmp/uploads/"

    @MockK
    private lateinit var likeRepository: LikeRepository

    @MockK
    private lateinit var pageable: Pageable

    private lateinit var myPageService: MyPageService
    private lateinit var user: User
    private lateinit var performanceList: List<Performance>
    private lateinit var emptyPerformanceList: List<Performance>
    private lateinit var performance1: Performance
    private lateinit var performance2: Performance
    private lateinit var place: Place

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        user = User(id = 1L, email = "test@test.com", username = "test")
        place = Place(3L, "서울특별시 서초구 서초동 1307", "공연장A", 500)

        performance1 = Performance(
            1L,
            "테스트 공연",
            "라따뚜이2",
            place,
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(3),
            Category.MOVIE,
            false,
            false,
            LocalDateTime.now().minusDays(1),
            File(10L, "001.jpg", "poster1.jpg")
        )

        performance2 = Performance(
            2L,
            "테스트 공연",
            "라따뚜이1",
            place,
            LocalDateTime.now().plusHours(4),
            LocalDateTime.now().plusHours(6),
            Category.MOVIE,
            false,
            false,
            LocalDateTime.now().minusDays(1),
            File(11L, "002.jpg", "poster2.jpg")
        )

        performanceList = listOf(performance1, performance2)
        emptyPerformanceList = emptyList()

        myPageService = MyPageService(
            likeRepository,
            UPLOAD
        )
    }

    @Test
    @DisplayName("찜한 목록이 있는 경우")
    fun get_liked_performance_by_userId() {

        every {
            likeRepository.findLikedPerformancesByUserId(eq(user.id!!), any())
        } returns PageImpl(performanceList)

        val result: Page<LikedPerformanceDto> =
            myPageService.getUserLikedPerformance(user.id!!, pageable)

        result.content shouldHaveSize 2
        result.content.first().id shouldBe performance1.id
        result.content[1].id shouldBe performance2.id
    }

    @Test
    @DisplayName("찜한 목록이 없는 경우")
    fun no_liked_performance_by_userId() {
        every {
            likeRepository.findLikedPerformancesByUserId(eq(user.id!!), any())
        } returns PageImpl(emptyPerformanceList)

        val result: Page<LikedPerformanceDto> = myPageService.getUserLikedPerformance(
            user.id!!,
            pageable
        )

        result.content.shouldBeEmpty()
    }
}
