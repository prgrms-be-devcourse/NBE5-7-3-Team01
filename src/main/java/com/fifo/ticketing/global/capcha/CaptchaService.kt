package com.fifo.ticketing.global.capcha

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

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