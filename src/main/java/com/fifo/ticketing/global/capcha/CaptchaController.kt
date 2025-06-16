package com.fifo.ticketing.global.capcha

import com.fifo.ticketing.global.capcha.dto.CaptchaResponse
import com.fifo.ticketing.global.capcha.dto.CaptchaVerifyRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

@RestController
@RequestMapping("/api/captcha")
class CaptchaController(
    private val captchaStorage: CaptchaStore
) {


    @GetMapping
    fun generateCaptcha(): CaptchaResponse{
        val randText = generateRandomText()
        val id = UUID.randomUUID().toString()

        captchaStorage[id] = randText;

        val image = createCaptchaImage(randText)
        val imageBase64 = Base64.getEncoder().encodeToString(image)

        return CaptchaResponse(id, "data:image/png;base64,$imageBase64")
    }
    @PostMapping("/verify")
    fun verifyCaptcha(@RequestBody req: CaptchaVerifyRequest) : ResponseEntity<String> {
        val expected = captchaStorage[req.captchaId] ?: return ResponseEntity.status(403).body("유효 시간이 만료되었습니다.")
        if(expected != req.input) return ResponseEntity.status(403).body("잘못된 인증 번호입니다.")

        captchaStorage.remove(req.captchaId)
        return ResponseEntity.ok("올바른 입력입니다.")
    }




}