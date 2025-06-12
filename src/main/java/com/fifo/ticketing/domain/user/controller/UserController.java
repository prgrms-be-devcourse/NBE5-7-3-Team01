package com.fifo.ticketing.domain.user.controller;

import com.fifo.ticketing.domain.book.service.BookService;
import com.fifo.ticketing.domain.performance.dto.LikedPerformanceDto;
import com.fifo.ticketing.domain.user.dto.SessionUser;
import com.fifo.ticketing.domain.user.dto.UserDto;
import com.fifo.ticketing.domain.user.entity.Role;
import com.fifo.ticketing.domain.user.service.AdminService;
import com.fifo.ticketing.domain.user.service.MyPageService;
import com.fifo.ticketing.global.util.UserValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 API")
public class UserController {

    private final MyPageService myPageService;
    private final AdminService adminService;
    private final BookService bookService;

    @GetMapping("/users/likes")
    @Operation(summary = "좋아요한 공연 목록 조회", description = "Session의 유저ID(userId)를 이용하여 좋아요한 공연 목록을 조회합니다.")
    public Page<LikedPerformanceDto> getUserLikes(HttpSession session,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "5", required = false) int size) {
        Pageable pageable = PageRequest.of(page, size);
        SessionUser loginUser = UserValidator.validateSessionUser(session);
        Long userId = loginUser.id;
        return myPageService.getUserLikedPerformance(
            userId, pageable
        );
    }

    @GetMapping("/users/list")
    @Operation(summary = "유저 목록 조회", description = "이름(name)과 권한(Role)을 이용하여 관리자가 유저 목록을 조회합니다.")
    public Page<UserDto> getUserList(HttpSession session,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "5", required = false) int size,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Role role) {
        Pageable pageable = PageRequest.of(page, size);
        return getUserData(name, role, pageable);
    }

    @PutMapping("/users/status/{userId}")
    @Operation(summary = "유저 상태 수정", description = "유저ID(userId)를 이용하여 사용자의 상태를 수정합니다.")
    public ResponseEntity<?> updateUserStatus(@PathVariable("userId") Long userId) {
        adminService.updateUserStatus(userId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "userId", userId,
                "message", "유저 정보가 수정되었습니다!"
        ));
    }

    private Page<UserDto> getUserData(String name, Role role, Pageable pageable) {
        if (name != null && role != null) {
            return adminService.getUsersByRoleAndName(pageable, role, name);
        } else if (name != null) {
            return adminService.getUsersByName(pageable, name);
        } else if (role != null) {
            return adminService.getUsersByRole(pageable, role);
        } else {
            return adminService.getAllUsers(pageable);
        }
    }

    @PostMapping("/users/books/{bookId}/paid")
    @Operation(summary = "예약 결제", description = "예약ID(booId)를 이용하여 해당 예약을 결제 상태로 수정합니다.")
    public ResponseEntity<?> completePayment(@PathVariable Long bookId) {
        bookService.completePayment(bookId);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "bookId", bookId,
            "message", "결제가 완료되었습니다!"
        ));
    }
}
