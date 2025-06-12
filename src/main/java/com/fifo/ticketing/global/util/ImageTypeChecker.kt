package com.fifo.ticketing.global.util

import org.springframework.web.multipart.MultipartFile

object ImageTypeChecker {

    @JvmStatic
    fun isImage(contentType: String?): Boolean {
        return contentType?.startsWith("image/") == true
    }

    @JvmStatic
    fun validImageExtension(file: MultipartFile): Boolean {
        val filename = file.originalFilename?.lowercase()
        return filename != null && (
                filename.endsWith(".jpg") ||
                        filename.endsWith(".jpeg") ||
                        filename.endsWith(".png") ||
                        filename.endsWith(".gif") ||
                        filename.endsWith(".webp")
                )
    }
}

