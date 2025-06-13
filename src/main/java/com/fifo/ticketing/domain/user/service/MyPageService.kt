package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.domain.like.repository.LikeRepository
import com.fifo.ticketing.domain.performance.dto.LikedPerformanceDto
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.mapper.PerformanceMapper
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MyPageService(
    private val likeRepository: LikeRepository,
    @Value("\${file.url-prefix}")
    private val urlPrefix: String
) {

    @Transactional(readOnly = true)
    fun getUserLikedPerformance(
        userId: Long?,
        pageable: Pageable
    ): Page<LikedPerformanceDto> {
        val id = userId ?: throw ErrorException(ErrorCode.NOT_FOUND_MEMBER)

        val likedPerformances: Page<Performance> =
            likeRepository.findLikedPerformancesByUserId(id, pageable)

        if (likedPerformances.isEmpty) {
            return Page.empty()
        }

        return PerformanceMapper.toPageLikedPerformanceDto(likedPerformances, urlPrefix)
    }
}
