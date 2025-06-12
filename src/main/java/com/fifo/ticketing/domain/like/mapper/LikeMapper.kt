package com.fifo.ticketing.domain.like.mapper

import com.fifo.ticketing.domain.like.entity.Like
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.user.entity.User

object LikeMapper {
    fun create(user: User, performance: Performance): Like {
        val like = Like(null, user, performance, true)
        return like
    }
}