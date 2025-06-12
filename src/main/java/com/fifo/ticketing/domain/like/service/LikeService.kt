package com.fifo.ticketing.domain.like.service

import com.fifo.ticketing.domain.like.dto.LikeRequest
import com.fifo.ticketing.domain.like.entity.Like
import com.fifo.ticketing.domain.like.entity.LikeCount
import com.fifo.ticketing.domain.like.repository.LikeCountRepository
import com.fifo.ticketing.domain.like.repository.LikeRepository
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import kotlin.math.max

@Service
class LikeService (
    private val likeRepository: LikeRepository,
    private val likeCountRepository: LikeCountRepository,
    private val userRepository: UserRepository,
    private val performanceRepository: PerformanceRepository

    ){

    @Transactional
    fun toggleLike(userId: Long, likeRequest: LikeRequest): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { ErrorException("존재하지 않는 회원입니다.", ErrorCode.NOT_FOUND_MEMBER) }

        val performance = performanceRepository.findById(likeRequest.performanceId)
            .orElseThrow { ErrorException("예매 가능한 공연이 존재하지 않습니다.", ErrorCode.NOT_FOUND_PERFORMANCES) }

        val existingLike = likeRepository.findByUserAndPerformance(user, performance)

        //like가 없다면
        if (existingLike == null) {
            val like = Like.builder()
                .user(user)
                .performance(performance)
                .isLiked(true)
                .build()
            likeRepository.save(like)
            updateLike(performance, 1)
            // likecount 제거
            return true
        } //없다ㅕㄴ


        if (existingLike.isLiked) {
            existingLike.isLiked = false
            likeRepository.save(existingLike)
            updateLike(performance, -1)
            // likeCOunt 추가
            return false
        }

        existingLike.isLiked = true
        likeRepository.save(existingLike)
        updateLike(performance, 1)
        return true
    }

    private fun updateLike(performance: Performance, cnt: Int) {
        val likeCount: LikeCount = likeCountRepository.findByPerformance(performance)?:
        throw  ErrorException("예매 가능한 공연이 존재하지 않습니다.",ErrorCode.NOT_FOUND_PERFORMANCES)

        var updatedCnt = likeCount.likeCount + cnt
        updatedCnt = max(0.0, updatedCnt.toDouble()).toLong() //  음수 방지
        likeCount.setLikeCount(updatedCnt)
        likeCountRepository.save(likeCount)
    }

    fun getLikedPerformancesIds(userId: Long): List<Long> {
        userRepository.findById(userId)?:
        throw ErrorException("존재하지 않는 회원입니다.", ErrorCode.NOT_FOUND_MEMBER)

        return likeRepository.findLikedPerformanceIdsByUserId(userId)
    }
}
