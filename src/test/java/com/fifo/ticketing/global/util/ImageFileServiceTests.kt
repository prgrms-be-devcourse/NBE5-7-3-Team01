package com.fifo.ticketing.global.util

import com.fifo.ticketing.global.entity.File
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import com.fifo.ticketing.global.repository.FileRepository
import com.fifo.ticketing.global.service.ImageFileService
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File as JavaFile
import java.io.IOException

@ExtendWith(MockitoExtension::class)
open class ImageFileServiceTests {
    @Mock
    private lateinit var fileRepository: FileRepository

    @Mock
    private lateinit var mockFile: MultipartFile

    @InjectMocks
    private lateinit var imageFileService: ImageFileService

    @BeforeEach
    fun setUp() {
        ReflectionTestUtils.setField(imageFileService, "uploadDir", "/test/uploads")
    }

    @Test
    @DisplayName("파일이 정상적으로 업로드 되는 경우")
    @Transactional
    @Throws(
        IOException::class
    )
    open fun test_upload_file_success() {
        // Given
        Mockito.`when`(mockFile!!.contentType).thenReturn("image/png")
        Mockito.`when`(mockFile.originalFilename).thenReturn("test.png")
        Mockito.doNothing().`when`(mockFile).transferTo(
            ArgumentMatchers.any(
                JavaFile::class.java
            )
        )
        val expectedFile =
            File(null, "generated-uuid.png", "test.png")
        Mockito.`when`(
            fileRepository!!.save(
                ArgumentMatchers.any(
                    File::class.java
                )
            )
        ).thenReturn(expectedFile) // 반드시 추가!

        // When
        val result = imageFileService!!.uploadFile(mockFile)
        Assertions.assertThat(result).isNotNull() // Null 체크

        // 4. 저장된 객체 검증
        val fileCaptor = ArgumentCaptor.forClass(
            File::class.java
        )
        Mockito.verify(fileRepository).save(fileCaptor.capture())
        val savedFile = fileCaptor.value

        // 5. 필드별 검증
        Assertions.assertThat(savedFile.originalFileName).isEqualTo("test.png")
        Assertions.assertThat(result.originalFileName).isEqualTo(savedFile.originalFileName)

        // 6. 확장자 검증 (안전한 방법)
        val fileName = savedFile.encodedFileName
        Assertions.assertThat(fileName).endsWith(".png") // 확장자만 확인
    }

    @Test
    @DisplayName("이미지 확장자가 아닌 경우 예외 발생")
    @Throws(
        Exception::class
    )
    fun test_invalid_file_type_throws_exception() {
        Mockito.`when`(mockFile!!.contentType).thenReturn("text/plain")

        val exception = org.junit.jupiter.api.Assertions.assertThrows(
            ErrorException::class.java
        ) {
            imageFileService!!.uploadFile(mockFile)
        }

        Assertions.assertThat(exception.errorCode).isEqualTo(ErrorCode.INVALID_IMAGE_TYPE)
    }
}