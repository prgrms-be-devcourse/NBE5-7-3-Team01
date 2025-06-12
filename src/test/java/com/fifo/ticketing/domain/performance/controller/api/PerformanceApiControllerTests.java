package com.fifo.ticketing.domain.performance.controller.api;

import com.fifo.ticketing.domain.like.entity.LikeCount;
import com.fifo.ticketing.domain.like.repository.LikeCountRepository;
import com.fifo.ticketing.domain.performance.entity.Grade;
import com.fifo.ticketing.domain.performance.entity.Performance;
import com.fifo.ticketing.domain.performance.entity.Place;
import com.fifo.ticketing.domain.performance.repository.GradeRepository;
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository;
import com.fifo.ticketing.domain.performance.repository.PlaceRepository;
import com.fifo.ticketing.domain.seat.repository.SeatRepository;
import com.fifo.ticketing.global.entity.File;
import com.fifo.ticketing.global.service.ImageFileService;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import static com.fifo.ticketing.domain.performance.entity.Category.MOVIE;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class PerformanceApiControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaceRepository placeRepository;
    @Autowired
    private GradeRepository gradeRepository;
    @Autowired
    private PerformanceRepository performanceRepository;
    @Autowired
    private LikeCountRepository likeCountRepository;
    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private ImageFileService imageFileService;

    private Place savedPlace;

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Autowired
    private SeatRepository seatRepository;

    @BeforeEach
    void setUp() throws IOException {

        Place place = Place.builder()
            .address("서울특별시 서초구 서초동 1307")
            .name("강남아트홀")
            .totalSeats(50)
            .build();
        savedPlace = placeRepository.save(place);

        Grade gradeS = Grade.builder()
            .place(savedPlace)
            .grade("S")
            .defaultPrice(120000)
            .seatCount(20)
            .build();

        Grade gradeA = Grade.builder()
            .place(savedPlace)
            .grade("A")
            .defaultPrice(90000)
            .seatCount(30)
            .build();

        gradeRepository.saveAll(List.of(gradeS, gradeA));
        when(imageFileService.uploadFile(any(MultipartFile.class))).thenReturn(File.builder().encodedFileName("test.webp").originalFileName("test.webp").build());
    }

    @DisplayName("@BeforeEach로 저장된 Place가 제대로 존재하는지 확인")
    @Test
    void test_setUp_place_exists() {
        Optional<Place> foundPlace = placeRepository.findById(savedPlace.getId());
        assertThat(foundPlace).isPresent();
        assertThat(foundPlace.get().getName()).isEqualTo("강남아트홀");
    }

    @DisplayName("@BeforeEach로 저장된 Grades가 제대로 존재하는지 확인")
    @Test
    void test_setUp_grades_exist() {
        List<Grade> foundGrades = gradeRepository.findAllByPlaceId(savedPlace.getId());
        assertThat(foundGrades).hasSize(2);
        assertThat(foundGrades.get(0).getGrade()).isEqualTo("S");
        assertThat(foundGrades.get(0).getPlace().getId()).isEqualTo(savedPlace.getId());
        assertThat(foundGrades.get(1).getGrade()).isEqualTo("A");
        assertThat(foundGrades.get(1).getPlace().getId()).isEqualTo(savedPlace.getId());
    }

    @DisplayName("H2 Database에 공연 등록이 성공하는 경우 (Mocking 사용)")
    @Test
    void test_performance_create_success_mocking() throws Exception {
        // Given
        String requestJson = """
            {
                "title": "라따뚜이",
                "description": "픽사의 명작 애니메이션",
                "category": "MOVIE",
                "performanceStatus": true,
                "startTime": "2025-06-01T19:00:00",
                "endTime": "2025-06-01T21:00:00",
                "reservationStartTime": "2025-05-12T19:00:00",
                "placeId": %d
            }
            """.formatted(savedPlace.getId());

        ClassPathResource resource = new ClassPathResource("uploads/default.webp");
        InputStream inputStream = resource.getInputStream();

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "default.webp",
            MediaType.IMAGE_JPEG_VALUE,
            inputStream
        );

        MockMultipartFile request = new MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/performances")
                .file(file)
                .file(request)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().string("공연이 등록되었습니다."));

        Performance savedPerformance = entityManager.createQuery("SELECT p FROM Performance p WHERE p.title = :title", Performance.class)
            .setParameter("title", "라따뚜이")
            .setMaxResults(1)
            .getSingleResult();
        assertThat(savedPerformance).isNotNull();

        List<LikeCount> foundLikeCounts = entityManager.createQuery("SELECT lc FROM LikeCount lc WHERE lc.performance = :performance", LikeCount.class)
            .setParameter("performance", savedPerformance)
            .getResultList();
        assertThat(foundLikeCounts).isNotEmpty();
        assertThat(foundLikeCounts.get(0).getLikeCount()).isEqualTo(0L);
    }

    @DisplayName("H2 Database에 공연 등록이 성공하는 경우 (cascade 설정 확인)")
    @Test
    void test_performance_create_success_cascade() throws Exception {
        // Given
        seatRepository.deleteAll();
        likeCountRepository.deleteAll();
        performanceRepository.deleteAll();

        String requestJson = """
        {
            "title": "라따뚜이",
            "description": "픽사의 명작 애니메이션",
            "category": "MOVIE",
            "performanceStatus": true,
            "startTime": "2025-06-01T19:00:00",
            "endTime": "2025-06-01T21:00:00",
            "reservationStartTime": "2025-05-12T19:00:00",
            "placeId": %d
        }
        """.formatted(savedPlace.getId());

        ClassPathResource resource = new ClassPathResource("uploads/default.webp");
        InputStream inputStream = resource.getInputStream();

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "default.webp",
            MediaType.IMAGE_JPEG_VALUE,
            inputStream
        );

        MockMultipartFile request = new MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.getBytes()
        );

        // ImageFileService Mocking (파일 업로드 성공 가정)
        File uploadedFile = File.builder().encodedFileName("encoded").originalFileName("default.webp").build();
        when(imageFileService.uploadFile(any(MultipartFile.class))).thenReturn(uploadedFile);

        // When & Then
        ResultActions resultActions = mockMvc.perform(multipart("/api/performances")
                .file(file)
                .file(request)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().string("공연이 등록되었습니다."));

        // 추가적인 검증: 저장된 Performance에 File이 연결되었는지 확인
        Performance savedPerformance = entityManager.createQuery("SELECT p FROM Performance p JOIN FETCH p.file WHERE p.title = :title", Performance.class)
            .setParameter("title", "라따뚜이")
            .setMaxResults(1) // 결과 수를 1로 제한
            .getSingleResult(); // 단일 결과를 가져옴
        assertThat(savedPerformance).isNotNull();
        assertThat(savedPerformance.getFile()).isNotNull();
        assertThat(savedPerformance.getFile().getEncodedFileName()).isEqualTo("encoded");

        List<LikeCount> foundLikeCounts = entityManager.createQuery("SELECT lc FROM LikeCount lc WHERE lc.performance = :performance", LikeCount.class)
            .setParameter("performance", savedPerformance)
            .getResultList();
        assertThat(foundLikeCounts).isNotEmpty();
        assertThat(foundLikeCounts.get(0).getLikeCount()).isEqualTo(0L);
    }

    @DisplayName("H2 Database에 저장된 공연 삭제 시 LikeCount는 삭제되지 않음")
    @Test
    void test_performance_delete_success_likeCount_remains() throws Exception {
        // Given: 공연 생성 및 저장
        Performance performance = Performance.builder()
            .title("공연 삭제 테스트")
            .description("테스트용 공연 설명")
            .category(MOVIE)
            .performanceStatus(true)
            .startTime(LocalDateTime.of(2025, 6, 1, 19, 0))
            .endTime(LocalDateTime.of(2025, 6, 1, 21, 0))
            .reservationStartTime(LocalDateTime.of(2025, 5, 20, 19, 0))
            .place(savedPlace)
            .build();
        performance = performanceRepository.save(performance);

        // LikeCount 생성 및 저장
        LikeCount likeCount = LikeCount.builder()
            .performance(performance)
            .likeCount(0L)
            .build();
        likeCountRepository.save(likeCount);

        Long performanceId = performance.getId();

        // When & Then: 공연 삭제 요청
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .delete("/api/performances/%d".formatted(performanceId))
            )
            .andExpect(status().isOk())
            .andExpect(content().string("공연이 삭제되었습니다."));

        // Then 1: Performance는 soft delete 처리됨
        Performance deletedPerformance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new IllegalStateException("공연이 존재하지 않음"));
        assertThat(deletedPerformance.isDeletedFlag()).isTrue();

        // Then 2: LikeCount는 여전히 존재함
        List<LikeCount> likeCounts = likeCountRepository.findAll();
        assertThat(likeCounts).isNotEmpty();
        assertThat(likeCounts.get(0).getPerformance().getId()).isEqualTo(performanceId);
    }
}