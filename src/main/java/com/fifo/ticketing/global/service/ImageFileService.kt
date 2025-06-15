package com.fifo.ticketing.global.service

import com.fifo.ticketing.global.entity.File
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import com.fifo.ticketing.global.repository.FileRepository
import com.fifo.ticketing.global.util.ImageTypeChecker.isImage
import com.fifo.ticketing.global.util.ImageTypeChecker.validImageExtension
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@Service
class ImageFileService(
    private val fileRepository: FileRepository
) {
    @Value("\${file.upload-dir}")
    lateinit var uploadDir: String

    @Transactional
    @Throws(IOException::class)
    fun uploadFile(file: MultipartFile): File {
        validateImageType(file)
        val originalFileName =
            file.originalFilename ?: throw ErrorException(ErrorCode.FILE_UPLOAD_FAILED)
        val uuidFileName = generateUuidFileName(originalFileName)
        val savePath = Paths.get(uploadDir, uuidFileName)

        try {
            file.transferTo(savePath.toFile())
            val generatedFile = File(
                encodedFileName = uuidFileName,
                originalFileName = originalFileName
            )
            return fileRepository.save(generatedFile)
        } catch (e: Exception) {
            handleFileUploadFailure(savePath)
            throw ErrorException(ErrorCode.FILE_UPLOAD_FAILED)
        }
    }

    @Transactional
    fun deleteFile(encodedFileName: String?) {
        if (!encodedFileName.isNullOrEmpty()) {
            val filePathToDelete = Paths.get(uploadDir, encodedFileName)
            try {
                if (Files.exists(filePathToDelete)) {
                    Files.delete(filePathToDelete)
                }
            } catch (e: IOException) {
                throw ErrorException(ErrorCode.FILE_DELETE_FAILED)
            }
        }
    }

    @Transactional
    @Throws(IOException::class)
    fun updateFile(existFile: File?, file: MultipartFile) {
        validateImageType(file)
        val originalFileName =
            file.originalFilename ?: throw ErrorException(ErrorCode.FILE_UPDATE_FAILED)
        val extension = getExtension(originalFileName)
        val uuidFileName = "${UUID.randomUUID()}.$extension"
        val savePath = Paths.get(uploadDir, uuidFileName)
        val oldEncodedFileName = existFile?.encodedFileName

        try {
            file.transferTo(savePath.toFile())
            oldEncodedFileName?.let { deleteFile(it) }

            if (existFile != null) {
                existFile.update(uuidFileName, originalFileName)
            } else {
                val newFile = File(
                    encodedFileName = uuidFileName,
                    originalFileName = originalFileName,
                )
                fileRepository.save(newFile)
            }
        } catch (e: Exception) {
            handleFileUploadFailure(savePath)
            throw ErrorException(ErrorCode.FILE_UPDATE_FAILED)
        }
    }

    fun generateUuidFileName(originalFileName: String): String {
        val extension = getExtension(originalFileName)
        return "${UUID.randomUUID()}.$extension"
    }

    private fun handleFileUploadFailure(savePath: Path) {
        val targetFile = savePath.toFile()
        if (targetFile.exists()) {
            targetFile.delete()
        }
    }

    private fun getExtension(originalFileName: String?): String {
        return originalFileName?.substringAfterLast('.', "") ?: ""
    }

    private fun validateImageType(file: MultipartFile) {
        if (!isImage(file.contentType) || !validImageExtension(file)) {
            throw ErrorException(ErrorCode.INVALID_IMAGE_TYPE)
        }
    }
}
