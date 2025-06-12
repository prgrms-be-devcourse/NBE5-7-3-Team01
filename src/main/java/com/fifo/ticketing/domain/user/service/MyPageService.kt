package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.domain.like.repository.LikeRepository
import com.fifo.ticketing.domain.performance.dto.LikedPerformanceDto
import com.fifo.ticketing.domain.performance.mapper.PerformanceMapper
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@RequiredArgsConstructor
class MyPageService {
    private val likeRepository: LikeRepository? = null

    @Value("\${file.url-prefix}")
    private val urlPrefix: String? = null

    @Transactional(readOnly = true)
    fun getUserLikedPerformance(
        userId: Long?,
        pageable: Pageable?
    ): Page<LikedPerformanceDto> {
        if (userId != null) {
            val likedPerformancesByUserId = likeRepository!!.findLikedPerformancesByUserId(
                userId, pageable
            )
            if (likedPerformancesByUserId.isEmpty) {
                return Page.empty()
            }
            return PerformanceMapper.toPageLikedPerformanceDto(
                likedPerformancesByUserId,
                urlPrefix
            )
        }
        throw ErrorException(ErrorCode.NOT_FOUND_MEMBER)
    }
}
