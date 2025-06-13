package com.fifo.ticketing.domain.like.service;

import com.fifo.ticketing.domain.like.dto.LikeRequest;
import com.fifo.ticketing.domain.like.entity.Like;
import com.fifo.ticketing.domain.like.entity.LikeCount;
import com.fifo.ticketing.domain.like.repository.LikeCountRepository;
import com.fifo.ticketing.domain.like.repository.LikeRepository;
import com.fifo.ticketing.domain.performance.entity.Category;
import com.fifo.ticketing.domain.performance.entity.Performance;
import com.fifo.ticketing.domain.performance.entity.Place;
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository;
import com.fifo.ticketing.domain.user.entity.User;
import com.fifo.ticketing.domain.user.repository.UserRepository;
import com.fifo.ticketing.global.entity.File;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;
    @Mock
    private LikeCountRepository likeCountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PerformanceRepository performanceRepository;

    private final Long userId = 1L;
    private final Long performanceId = 100L;

    private User user;
    private Place place;
    private File mockFile;
    private Performance performance;
    private LikeCount likeCount;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder().id(1L).build();
        place = new Place(1L, "서울특별시 서초구 서초동 1307", "강남아트홀", 100);

        mockFile = File.builder()
                .id(1L)
                .encodedFileName("poster.jpg")
                .originalFileName("sample.jpg")
                .build();

        performance = new Performance(
                100L, "라따뚜이", "라따뚜이는 픽시의 영화입니다.", place,
                LocalDateTime.of(2025, 6, 1, 19, 0),
                LocalDateTime.of(2025, 6, 1, 21, 0),
                Category.MOVIE,
                false,
                false,
                LocalDateTime.of(2025, 5, 12, 19, 0),
                mockFile
        );

        likeCount = LikeCount.builder().performance(performance).likeCount(1L).build();
    }

    @Test
    void 좋아요_처음_누를_경우() {

        LikeRequest request = new LikeRequest(performance.getId());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(performanceRepository.findById(performance.getId())).thenReturn(Optional.of(performance));
        when(likeRepository.findByUserAndPerformance(user, performance)).thenReturn(null);
        when(likeCountRepository.findByPerformance(performance)).thenReturn(Optional.of(likeCount));

        boolean result = likeService.toggleLike(userId, request);

        assertThat(result).isTrue();
        verify(likeRepository).save(any(Like.class));
        assertThat(likeCount.getLikeCount()).isEqualTo(2L);
    }

    @Test
    void 이미_좋아요를_누른_상태에서_취소하는_경우() {
        LikeRequest request = new LikeRequest(performance.getId());

        Like existingLike = Like.builder()
                .user(user)
                .performance(performance)
                .isLiked(true)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(performanceRepository.findById(performance.getId())).thenReturn(Optional.of(performance));
        when(likeRepository.findByUserAndPerformance(user, performance)).thenReturn(existingLike);
        when(likeCountRepository.findByPerformance(performance)).thenReturn(Optional.of(likeCount));

        boolean result = likeService.toggleLike(userId,request);

        assertThat(result).isFalse();
        assertThat(existingLike.isLiked()).isFalse();
        assertThat(likeCount.getLikeCount()).isEqualTo(0L);

        verify(likeRepository).save(existingLike);
        verify(likeCountRepository).save(likeCount);
    }

    @Test
    void 좋아요_취소한상태에서_다시_좋아요를_누르는_경우(){
        LikeRequest request = new LikeRequest( performance.getId());

        Like existingLike = Like.builder()
                .user(user)
                .performance(performance)
                .isLiked(false)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(performanceRepository.findById(performance.getId())).thenReturn(Optional.of(performance));
        when(likeRepository.findByUserAndPerformance(user, performance)).thenReturn(existingLike);
        when(likeCountRepository.findByPerformance(performance)).thenReturn(Optional.of(likeCount));

        boolean result = likeService.toggleLike(userId,request);
        assertThat(result).isTrue();
        assertThat(existingLike.isLiked()).isTrue();
        assertThat(likeCount.getLikeCount()).isEqualTo(2L);

        verify(likeRepository).save(existingLike);
        verify(likeCountRepository).save(likeCount);
    }
}