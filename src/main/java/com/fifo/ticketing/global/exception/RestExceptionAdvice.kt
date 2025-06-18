package com.fifo.ticketing.global.exception

import org.springframework.web.bind.annotation.RestControllerAdvice
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.HandlerMethod


@RestControllerAdvice
class RestExceptionAdvice {
    private val log = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException
    ): Any {
        // Class Level Error Handling (현재는 @ValidPerformanceDates에 해당)
        val classLevelError = ex.bindingResult.globalErrors.firstOrNull()
        val fieldError = ex.bindingResult.fieldErrors.firstOrNull()
        val message = when {
            classLevelError != null -> classLevelError.defaultMessage
            fieldError != null -> fieldError.defaultMessage
            else -> "잘못된 요청입니다."
        }

        log.warn("Validation Error: {}", message)


        val errorCode = when(message) {
            "INVALID_DATETIME_PERIOD" -> ErrorCode.INVALID_DATETIME_PERIOD
            "INVALID_DATETIME_RESERVATION" -> ErrorCode.INVALID_DATETIME_RESERVATION
            else -> ErrorCode.INVALID_DATETIME_TYPE
        }

        val httpStatus = when (errorCode.errorStatus) {
            ErrorStatus.NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorStatus.CONFLICT -> HttpStatus.CONFLICT
            ErrorStatus.INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
            ErrorStatus.ALREADY_EXISTS,
            ErrorStatus.BAD_REQUEST -> HttpStatus.BAD_REQUEST
            ErrorStatus.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED
        }

        val response = ErrorResponse<Nothing>(
            code = errorCode.code,
            message = errorCode.message
        )

        return ResponseEntity.status(httpStatus).body(response)
    }
}