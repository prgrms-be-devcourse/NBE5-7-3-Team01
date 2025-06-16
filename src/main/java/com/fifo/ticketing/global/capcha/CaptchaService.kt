package com.fifo.ticketing.global.capcha

import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

@Component
class CaptchaStore {
    val storage = ConcurrentHashMap<String, String>() // 임시 저장 위해

    operator fun set(id: String, value: String) {
        storage[id] = value
    }

    operator fun get(id: String): String? {
        return storage[id]
    }

    fun remove(id: String) {
        storage.remove(id)
    }
}

fun createCaptchaImage(text: String): ByteArray{ //캡차 이미지 바이트 배열로 생성
    val img = BufferedImage(150, 50, BufferedImage.TYPE_INT_RGB)
    val g = img.createGraphics()
    g.color = Color.white
    g.fillRect(0,0, 150, 50)
    g.color = Color.black
    g.font = Font("Arial", Font.BOLD, 32)
    g.drawString(text, 20, 35)
    g.dispose()

    val baos = ByteArrayOutputStream()
    ImageIO.write(img, "png", baos)
    return baos.toByteArray()
}

fun generateRandomText(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ123456789"
    return (1..6).map { chars.random() }.joinToString("")
}