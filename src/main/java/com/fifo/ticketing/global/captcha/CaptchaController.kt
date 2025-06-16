package com.fifo.ticketing.global.captcha

import com.fifo.ticketing.global.service.RedisService
import com.fifo.ticketing.global.captcha.dto.CaptchaResponse
import com.fifo.ticketing.global.captcha.dto.CaptchaVerifyRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/captcha")
class CaptchaController(
    private val captchaStorage: RedisService
) {

    @GetMapping
    fun generateCaptcha(): CaptchaResponse {
        val randomText = generateRandomText()
        val id = UUID.randomUUID().toString()
        val key = "captcha:$id"

        captchaStorage.setValuesWithTimeout(key, randomText, 3 * 60 * 1000)

        val image = createCaptchaImage(randomText)
        val imageBase64 = Base64.getEncoder().encodeToString(image)

        return CaptchaResponse(key, "data:image/png;base64,$imageBase64")
    }

    @PostMapping("/verify")
    fun verifyCaptcha(@RequestBody req: CaptchaVerifyRequest): ResponseEntity<String> {
        val expected = captchaStorage.getValues(req.captchaId) ?: return ResponseEntity.status(403)
            .body("유효 시간이 만료되었습니다.")
        if (expected != req.input) return ResponseEntity.status(403).body("잘못된 인증 번호입니다.")

        captchaStorage.deleteValues(req.captchaId)
        return ResponseEntity.ok("올바른 입력입니다.")
    }

}
