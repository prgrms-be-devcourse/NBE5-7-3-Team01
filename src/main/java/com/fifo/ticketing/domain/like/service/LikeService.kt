package com.fifo.ticketing.domain.like.service

import com.fifo.ticketing.domain.like.dto.LikeRequest
import com.fifo.ticketing.domain.like.entity.Like
import com.fifo.ticketing.domain.like.entity.LikeCount
import com.fifo.ticketing.domain.like.mapper.LikeMapper
import com.fifo.ticketing.domain.like.repository.LikeCountRepository
import com.fifo.ticketing.domain.like.repository.LikeRepository
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.math.max

@Service
class LikeService(
    private val likeRepository: LikeRepository,
    private val likeCountRepository: LikeCountRepository,
    private val userRepository: UserRepository,
    private val performanceRepository: PerformanceRepository
) {

    @Transactional
    fun toggleLike(userId: Long, likeRequest: LikeRequest): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow {
                ErrorException(
                    ErrorCode.NOT_FOUND_MEMBER,

                )
            }

        val performance = performanceRepository.findById(likeRequest.performanceId)
            .orElseThrow {
                ErrorException(
                    ErrorCode.NOT_FOUND_PERFORMANCES

                )
            }

        val existingLike = likeRepository.findByUserAndPerformance(user, performance)

        return if (existingLike == null) {
            val like = LikeMapper.create(user, performance)
            likeRepository.save(like)
            updateLike(performance, 1)
            true
        } else {
            val isNowLiked = !existingLike.isLiked
            existingLike.setLiked(isNowLiked)
            likeRepository.save(existingLike)
            updateLike(performance, if (isNowLiked) 1 else -1)
            isNowLiked
        }
    }

    private fun updateLike(performance: Performance, cnt: Int) {
        val likeCount = likeCountRepository.findByPerformance(performance)?:
        throw  ErrorException(ErrorCode.NOT_FOUND_PERFORMANCES)

        val updatedCnt = max(0L, likeCount.getLikeCount() + cnt)
        likeCount.setLikeCount(updatedCnt)
        likeCountRepository.save(likeCount)
    }

    fun getLikedPerformancesIds(userId: Long): List<Long> {
        userRepository.findById(userId)
            .orElseThrow {
                ErrorException(
                    ErrorCode.NOT_FOUND_MEMBER
                )
            }

        return likeRepository.findLikedPerformanceIdsByUserId(userId)
    }
}