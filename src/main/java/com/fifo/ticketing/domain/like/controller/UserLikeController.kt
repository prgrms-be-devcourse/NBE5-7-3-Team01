package com.fifo.ticketing.domain.like.controller

import com.fifo.ticketing.domain.like.dto.LikeRequest
import com.fifo.ticketing.domain.like.service.LikeService
import com.fifo.ticketing.global.util.UserValidator.validateSessionUser
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user/like")
class UserLikeController(
    private val likeService: LikeService
) {
    @PostMapping
    fun toggleLike(
        @RequestBody likeRequest: LikeRequest,
        httpSession: HttpSession
    ): ResponseEntity<String> {
        //LoginSuccessHandler에서 SessionUser로 저장했기 때문에
        val sessionUser = validateSessionUser(httpSession)


        val liked = likeService.toggleLike(sessionUser.id, likeRequest)
        return ResponseEntity.ok(if (liked) "Liked" else "Unliked")
    }
}

