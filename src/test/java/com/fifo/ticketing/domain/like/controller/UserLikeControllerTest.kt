package com.fifo.ticketing.domain.like.controller

import com.fifo.ticketing.domain.like.dto.LikeRequest
import com.fifo.ticketing.domain.like.service.LikeService
import com.fifo.ticketing.domain.user.dto.SessionUser
import com.fifo.ticketing.domain.user.entity.Role
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@WebMvcTest(UserLikeController::class)
@ActiveProfiles("ci")
class UserLikeControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var likeService: LikeService

    @Test
    @WithMockUser(username = "test", roles = ["USER"])
    fun `좋아요 요청시 Liked 반환`() {
        val likeRequest = LikeRequest(1L)

        val sessionUser = SessionUser(
            id = 1,
            username = "test",
            role = Role.USER
        )

        val session = MockHttpSession().apply {
            setAttribute("loginUser", sessionUser)  // ✅ 정확한 키
        }

        given(likeService.toggleLike(1L, likeRequest)).willReturn(true)

        mockMvc.perform(
            post("/user/like")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"performanceId": 1}""")
            // 세션 주입!
        )
            .andExpect(status().isOk)
            .andExpect(content().string("Liked"))
    }


    @Test
    @WithMockUser(username = "test", roles = ["USER"])
    fun `좋아요가 이미 존재하면 Unliked 반환`() {
        // given
        val likeRequest = LikeRequest(1L)

        val sessionUser = SessionUser(
            id = 1,
            username = "test",
            role = Role.USER
        )

        val session = MockHttpSession().apply {
            setAttribute("loginUser", sessionUser)
        }

        // 서비스가 false 반환 (== 좋아요 취소됨)
        given(likeService.toggleLike(1L, likeRequest)).willReturn(false)

        // when & then
        mockMvc.perform(
            post("/user/like")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"performanceId": 1}""")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("Unliked"))
    }


}