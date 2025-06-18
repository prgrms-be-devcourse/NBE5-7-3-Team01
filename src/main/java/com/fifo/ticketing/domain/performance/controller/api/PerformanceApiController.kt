package com.fifo.ticketing.domain.performance.controller.api

import com.fifo.ticketing.domain.performance.dto.PerformanceRequestDto
import com.fifo.ticketing.domain.performance.service.AdminPerformanceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Validated
@RestController
@RequestMapping("/api/performances")
@Tag(name = "Admin_Post", description = "관리자에 의한 공연 API")
class PerformanceApiController(
    private val adminPerformanceService: AdminPerformanceService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "공연 등록", description = "이미지 파일(file)과 공연 정보(request)를 이용하여 공연을 신규 등록합니다.")
    @Throws(
        IOException::class
    )
    fun createPerformance(
        @RequestPart("file") file: MultipartFile,
        @Valid @RequestPart("request") request: PerformanceRequestDto
    ): ResponseEntity<*> {
        val performance = adminPerformanceService.createPerformance(request, file)
        return ResponseEntity.ok("공연이 등록되었습니다.")
    }

    @PutMapping(value = ["/{id}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "공연 수정",
        description = "공연ID(id)와 이미지 파일(file), 그리고 공연 정보(request)를 이용하여 공연을 수정합니다."
    )
    @Throws(
        IOException::class
    )
    fun updatePerformance(
        @PathVariable id: Long,
        @RequestPart(value = "file", required = false) file: MultipartFile?,
        @Valid @RequestPart("request") request: PerformanceRequestDto
    ): ResponseEntity<*> {
        adminPerformanceService.updatePerformance(id, request, file)
        return ResponseEntity.ok("공연이 수정되었습니다.")
    }

    @DeleteMapping(value = ["/{id}"])
    @Operation(summary = "공연 삭제", description = "공연ID(id)를 이용하여 공연을 삭제합니다.")
    fun deletePerformance(@PathVariable id: Long): ResponseEntity<*> {
        adminPerformanceService.deletePerformance(id)
        return ResponseEntity.ok("공연이 삭제되었습니다.")
    }
}
