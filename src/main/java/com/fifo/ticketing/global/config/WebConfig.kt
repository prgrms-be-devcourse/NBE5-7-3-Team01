package com.fifo.ticketing.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebConfig(
    @Value("\${file.upload-dir}")
    private val uploadDir: String,

    @Value("\${file.url-prefix}")
    private val urlPrefix: String
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        var normalizedPath = Paths.get(uploadDir).normalize().toString()
        // Windows 경로가 역슬래시로 시작하는 경우 처리
        if (normalizedPath.startsWith("\\")) {
            normalizedPath = normalizedPath.substring(1)
        }
        // 경로를 URI 형식으로 변환
        var externalPath = "file:" + normalizedPath.replace("\\", "/")
        // 끝에 슬래시가 없으면 추가
        if (!externalPath.endsWith("/")) {
            externalPath += "/"
        }
        registry.addResourceHandler("$urlPrefix**")
            .addResourceLocations(externalPath)
    }
}
