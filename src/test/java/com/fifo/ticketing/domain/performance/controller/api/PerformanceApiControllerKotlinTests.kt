package com.fifo.ticketing.domain.performance.controller.api

import com.fifo.ticketing.domain.like.entity.LikeCount
import com.fifo.ticketing.domain.like.repository.LikeCountRepository
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Grade
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.entity.Place
import com.fifo.ticketing.domain.performance.repository.GradeRepository
import com.fifo.ticketing.domain.performance.repository.PerformanceAdminRepository
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository
import com.fifo.ticketing.domain.performance.repository.PlaceRepository
import com.fifo.ticketing.domain.seat.repository.SeatRepository
import com.fifo.ticketing.global.entity.File
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import com.fifo.ticketing.global.service.ImageFileService
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.InputStream
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class PerformanceApiControllerKotlinTests {

    @Autowired
    private lateinit var performanceAdminRepository: PerformanceAdminRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var placeRepository: PlaceRepository

    @Autowired
    private lateinit var gradeRepository: GradeRepository

    @Autowired
    private lateinit var performanceRepository: PerformanceRepository

    @Autowired
    private lateinit var likeCountRepository: LikeCountRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @MockitoBean
    private lateinit var imageFileService: ImageFileService

    private lateinit var savedPlace: Place
    private lateinit var mockFile: File

    @Value("\${file.upload-dir}")
    private lateinit var uploadDir: String


    @BeforeEach
    fun setUp() {
        val place = Place(null, "서울특별시 서초구 서초동 1307", "강남아트홀", 50)
        savedPlace = placeRepository.save(place)

        val gradeS = Grade(null, place, "S", 120000, 20)
        val gradeA = Grade(null, place, "A", 90000, 30)

        mockFile = File(null, "poster.jpg", "sample.jpg")

        gradeRepository.saveAll(listOf(gradeS, gradeA))
        whenever(imageFileService.uploadFile(any())).thenReturn(
            File(
                null,
                "encoded.webp",
                "test.webp"
            )
        )
    }

    @Test
    @DisplayName("@BeforeEach로 저장된 Place가 제대로 존재하는지 확인")
    fun `test setup place exists`() {
        val found = placeRepository.findById(savedPlace.id!!)
        assertThat(found).isPresent
        assertThat(found.get().name).isEqualTo(savedPlace.name)
    }

    @Test
    @DisplayName("BeforeEach로 저장된 Grades가 제대로 존재하는지 확인")
    fun `test setup grades exists`() {
        val list = gradeRepository.findAllByPlaceId(savedPlace.id!!)
        assertThat(list).hasSize(2)
        assertThat(list.map { it.grade }).containsExactlyInAnyOrder("S", "A")
        assertThat(list.all { it.place!!.id == savedPlace.id!! }).isTrue()
    }

    @Test
    @DisplayName("H2 Database에 공연 등록이 성공하는 경우 (Mocking 사용)")
    fun `performance create success mocking`() {
        val requestJson = """
            {
              "title": "라따뚜이1",
              "description": "픽사의 명작 애니메이션1",
              "category": "MOVIE",
              "performanceStatus": true,
              "startTime": "2025-06-01T19:00:00",
              "endTime": "2025-06-01T21:00:00",
              "reservationStartTime": "2025-05-12T19:00:00",
              "placeId": ${savedPlace.id}
            }
        """.trimIndent()

        val resource: InputStream = ClassPathResource("uploads/default.webp").inputStream
        val filePart =
            MockMultipartFile("file", "default.webp", MediaType.IMAGE_JPEG_VALUE, resource)
        val jsonPart =
            MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson.toByteArray()
            )

        mockMvc.perform(
            multipart("/api/performances").file(filePart).file(jsonPart)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
            .andExpect(status().isOk)
            .andExpect(
                content().string("공연이 등록되었습니다.")
            )

        val saved = entityManager.createQuery(
            "SELECT p FROM Performance p JOIN FETCH p.file WHERE p.title = :title",
            Performance::class.java
        )
            .setParameter("title", "라따뚜이1")
            .singleResult
        assertThat(saved.file).isNotNull
        assertThat(saved.file!!.encodedFileName).isEqualTo("encoded.webp")

        val likeCounts = entityManager.createQuery(
            "SELECT lc FROM LikeCount lc WHERE lc.performance = :performance", LikeCount::class.java
        )
            .setParameter("performance", saved)
            .resultList
        assertThat(likeCounts).isNotEmpty
        assertThat(likeCounts[0].likeCount).isEqualTo(0L)
    }


    @Test
    @DisplayName("H2 Database에 공연 등록이 성공하는 경우 (cascade 설정 확인)")
    fun `performance create success cascade`() {
        seatRepository.deleteAll()
        likeCountRepository.deleteAll()
        performanceRepository.deleteAll()

        val requestJson = """
            {
              "title": "라따뚜이",
              "description": "픽사의 명작 애니메이션",
              "category": "MOVIE",
              "performanceStatus": true,
              "startTime": "2025-06-01T19:00:00",
              "endTime": "2025-06-01T21:00:00",
              "reservationStartTime": "2025-05-12T19:00:00",
              "placeId": ${savedPlace.id}
            }
        """.trimIndent()

        val resource = ClassPathResource("uploads/default.webp").inputStream
        val filePart =
            MockMultipartFile("file", "default.webp", MediaType.IMAGE_JPEG_VALUE, resource)
        val jsonPart = MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.toByteArray()
        )

        whenever(imageFileService.uploadFile(any())).thenReturn(
            File(
                null,
                "encoded.webp",
                "default.webp"
            )
        )

        mockMvc.perform(
            multipart("/api/performances")
                .file(filePart)
                .file(jsonPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isOk)
            .andExpect(content().string("공연이 등록되었습니다."))

        val saved = entityManager.createQuery(
            "SELECT p FROM Performance p JOIN FETCH p.file WHERE p.title = :title",
            Performance::class.java
        )
            .setParameter("title", "라따뚜이")
            .singleResult
        assertThat(saved.file).isNotNull
        assertThat(saved.file!!.encodedFileName).isEqualTo("encoded.webp")

        val likeCounts = entityManager.createQuery(
            "SELECT lc FROM LikeCount lc WHERE lc.performance = :performance", LikeCount::class.java
        )
            .setParameter("performance", saved)
            .resultList
        assertThat(likeCounts).isNotEmpty
        assertThat(likeCounts[0].likeCount).isEqualTo(0L)
    }

    @Test
    @DisplayName("H2 Database에 저장된 공연 삭제 시 LikeCount는 삭제되지 않음")
    fun `test performance delete success likeCount remains`() {
        likeCountRepository.deleteAll()
        val performance = Performance(
            null,
            "공연 삭제 테스트",
            "테스트용 공연 설명",
            savedPlace,
            LocalDateTime.of(2025, 6, 1, 19, 0),
            LocalDateTime.of(2025, 6, 1, 21, 0),
            Category.MOVIE,
            true,
            false,
            LocalDateTime.of(2025, 5, 1, 19, 0),
            mockFile
        )
        val saved = performanceAdminRepository.save(performance)
        val savedCount = likeCountRepository.save(LikeCount(null, saved, 0L))

        mockMvc.perform(delete("/api/performances/${saved.id}"))
            .andExpect(status().isOk)
            .andExpect(content().string("공연이 삭제되었습니다."))

        val deletedPerformance = performanceAdminRepository.findById(saved.id)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_PERFORMANCE) }
        assertThat(deletedPerformance.deletedFlag).isTrue()

        val likeCounts = likeCountRepository.findAll()
        assertThat(likeCounts).isNotEmpty
        assertThat(likeCounts[0].performance.id).isEqualTo(saved.id)
    }

    @Test
    @DisplayName("H2 Database에 공연 수정이 성공한 경우 (Mocking 사용)")
    fun `performance update success mocking`() {
        val requestJson = """
            {
              "title": "라따뚜이",
              "description": "픽사의 명작 애니메이션",
              "category": "MOVIE",
              "performanceStatus": true,
              "startTime": "2025-06-01T19:00:00",
              "endTime": "2025-06-01T21:00:00",
              "reservationStartTime": "2025-05-12T19:00:00",
              "placeId": ${savedPlace.id}
            }
        """.trimIndent()

        val resource: InputStream = ClassPathResource("uploads/default.webp").inputStream
        val filePart =
            MockMultipartFile("file", "default.webp", MediaType.IMAGE_JPEG_VALUE, resource)
        val jsonPart =
            MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson.toByteArray()
            )

        mockMvc.perform(
            multipart("/api/performances")
                .file(filePart)
                .file(jsonPart)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
            .andExpect(status().isOk)
            .andExpect(
                content().string("공연이 등록되었습니다.")
            )

        val saved = entityManager.createQuery(
            "SELECT p FROM Performance p JOIN FETCH p.file WHERE p.title = :title",
            Performance::class.java
        )
            .setParameter("title", "라따뚜이")
            .singleResult
        assertThat(saved.file).isNotNull
        assertThat(saved.file!!.encodedFileName).isEqualTo("encoded.webp")

        val likeCounts = entityManager.createQuery(
            "SELECT lc FROM LikeCount lc WHERE lc.performance = :performance", LikeCount::class.java
        )
            .setParameter("performance", saved)
            .resultList
        assertThat(likeCounts).isNotEmpty
        assertThat(likeCounts[0].likeCount).isEqualTo(0L)

        val updateRequestJson = """
            {
              "title": "수정된 제목",
              "description": "수정된 설명입니다.",
              "category": "MOVIE",
              "performanceStatus": true,
              "startTime": "2025-06-01T19:00:00",
              "endTime": "2025-06-01T21:00:00",
              "reservationStartTime": "2025-05-12T19:00:00",
              "placeId": ${savedPlace.id}
            }
        """.trimIndent()

        val updateJsonPart =
            MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                updateRequestJson.toByteArray()
            )

        mockMvc.perform(
            multipart(HttpMethod.PUT, "/api/performances/${saved.id}")
                .file(filePart)
                .file(updateJsonPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isOk)
            .andExpect(content().string("공연이 수정되었습니다."))

        val updated = performanceRepository.findById(saved.id!!)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_PERFORMANCE) }

        assertThat(updated.title).isEqualTo("수정된 제목")
        assertThat(updated.description).isEqualTo("수정된 설명입니다.")
    }

}