package com.fifo.ticketing.domain.user.controller.api

import com.fifo.ticketing.domain.book.service.BookService
import com.fifo.ticketing.domain.performance.dto.LikedPerformanceDto
import com.fifo.ticketing.domain.user.dto.UserDto
import com.fifo.ticketing.domain.user.entity.Role
import com.fifo.ticketing.domain.user.service.AdminService
import com.fifo.ticketing.domain.user.service.MyPageService
import com.fifo.ticketing.global.util.UserValidator.validateSessionUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpSession
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name = "User", description = "유저 API")
class UserController(
    private val myPageService: MyPageService,
    private val adminService: AdminService,
    private val bookService: BookService
) {

    @GetMapping("/users/likes")
    @Operation(
        summary = "좋아요한 공연 목록 조회",
        description = "Session의 유저ID(userId)를 이용하여 좋아요한 공연 목록을 조회합니다."
    )
    fun getUserLikes(
        session: HttpSession,
        @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
        @RequestParam(value = "size", defaultValue = "5", required = false) size: Int
    ): Page<LikedPerformanceDto> {
        val pageable: Pageable = PageRequest.of(page, size)
        val loginUser = validateSessionUser(session)

        return myPageService.getUserLikedPerformance(loginUser.id, pageable)
    }

    @GetMapping("/users/list")
    @Operation(summary = "유저 목록 조회", description = "이름(name)과 권한(Role)을 이용하여 관리자가 유저 목록을 조회합니다.")
    fun getUserList(
        session: HttpSession?,
        @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
        @RequestParam(value = "size", defaultValue = "5", required = false) size: Int,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) role: Role?
    ): Page<UserDto> {
        val pageable: Pageable = PageRequest.of(page, size)
        return when {
            name != null && role != null -> adminService.getUsersByRoleAndName(pageable, role, name)
            name != null -> adminService.getUsersByName(pageable, name)
            role != null -> adminService.getUsersByRole(pageable, role)
            else -> adminService.getAllUsers(pageable)
        }
    }

    @PutMapping("/users/status/{userId}")
    @Operation(summary = "유저 상태 수정", description = "유저ID(userId)를 이용하여 사용자의 상태를 수정합니다.")
    fun updateUserStatus(@PathVariable("userId") userId: Long): ResponseEntity<kotlin.collections.Map<String, Any>> {
        adminService.updateUserStatus(userId)
        return ResponseEntity.ok(
            mapOf(
                "status" to "success",
                "userId" to userId,
                "message" to "유저 정보가 수정되었습니다!"
            )
        )
    }

    @PostMapping("/users/books/{bookId}/paid")
    @Operation(summary = "예약 결제", description = "예약ID(booId)를 이용하여 해당 예약을 결제 상태로 수정합니다.")
    fun completePayment(@PathVariable bookId: Long): ResponseEntity<kotlin.collections.Map<String, Any>> {
        bookService.completePayment(bookId)
        return ResponseEntity.ok(
            mapOf(
                "status" to "success",
                "bookId" to bookId,
                "message" to "결제가 완료되었습니다!"
            )
        )
    }
}
