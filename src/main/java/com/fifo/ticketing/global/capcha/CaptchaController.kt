package com.fifo.ticketing.global.capcha

import com.fifo.ticketing.global.capcha.dto.CaptchaResponse
import com.fifo.ticketing.global.capcha.dto.CaptchaVerifyRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/captcha")
class CaptchaController(
    private val captchaStorage: CaptchaStore
) {


    @GetMapping
    fun generateCaptcha(): CaptchaResponse {
        val randomText = generateRandomText()
        val id = UUID.randomUUID().toString()

        captchaStorage[id] = randomText;

        val image = createCaptchaImage(randomText)
        val imageBase64 = Base64.getEncoder().encodeToString(image)

        return CaptchaResponse(id, "data:image/png;base64,$imageBase64")
    }

    @PostMapping("/verify")
    fun verifyCaptcha(@RequestBody req: CaptchaVerifyRequest): ResponseEntity<String> {
        val expected = captchaStorage[req.captchaId] ?: return ResponseEntity.status(403)
            .body("유효 시간이 만료되었습니다.")
        if (expected != req.input) return ResponseEntity.status(403).body("잘못된 인증 번호입니다.")

        captchaStorage.remove(req.captchaId)
        return ResponseEntity.ok("올바른 입력입니다.")
    }


}