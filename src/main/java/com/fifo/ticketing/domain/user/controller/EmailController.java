package com.fifo.ticketing.domain.user.controller;

import com.fifo.ticketing.domain.user.dto.form.AuthEmailRequest;
import com.fifo.ticketing.domain.user.dto.form.SendEmailRequest;
import com.fifo.ticketing.domain.user.service.EmailAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Email", description = "이메일 인증 API")
public class EmailController {

    private final EmailAuthService emailAuthService;

    @PostMapping("/email/send")
    @Operation(summary = "공연 등록", description = "이메일 정보(sendEmailRequest)를 이용하여 인증 이메일을 송신합니다.")
    public ResponseEntity<?> emailSend(@RequestBody SendEmailRequest sendEmailRequest)
        throws MessagingException {
        emailAuthService.sendEmail(sendEmailRequest.email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/auth")
    @Operation(summary = "공연 등록", description = "이메일 정보(sendEmailRequest)를 이용하여 인증 이메일을 송신합니다.")
    public ResponseEntity<?> emailAuth(@RequestBody AuthEmailRequest authEmailRequest,
        HttpSession session) {
        boolean checked = emailAuthService.checkAuthCode(authEmailRequest.email,
            authEmailRequest.authCode,
            session);
        if (checked) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

