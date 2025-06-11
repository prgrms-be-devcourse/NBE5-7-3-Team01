package com.fifo.ticketing.global.exception

import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.NoHandlerFoundException

@ControllerAdvice
class ExceptionAdvice {

    private val log = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(ErrorException::class, AlertDetailException::class)
    fun handleException(
        ex: RuntimeException,
        model: Model,
        handlerMethod: HandlerMethod
    ): Any {
        val isApiRequest =
            AnnotatedElementUtils.hasAnnotation(handlerMethod.method, ResponseBody::class.java)

        return when (ex) {
            is ErrorException -> handleCommonException(
                ex.errorCode,
                ex.url,
                isApiRequest,
                model,
                ex
            )

            is AlertDetailException -> handleCommonException(
                ex.errorCode,
                ex.url,
                isApiRequest,
                model,
                ex
            )

            else -> {
                log.error("Unhandled exception: ", ex)
                if (isApiRequest) {
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ErrorResponse<Nothing>("500", "Unexpected error occurred"))
                } else {
                    model.addAttribute("message", "Unexpected error occurred")
                    model.addAttribute("url", "/")
                    "error/alert"
                }
            }
        }
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handlePageNotFoundException(ex: NoHandlerFoundException, model: Model): String {
        model.addAttribute("message", "404 Page not found")
        return "error/404"
    }

    private fun handleCommonException(
        errorCode: ErrorCode,
        url: String?,
        isApiRequest: Boolean,
        model: Model,
        ex: Exception
    ): Any {
        log.error(errorCode.message, ex)

        return if (isApiRequest) {
            val httpStatus = when (errorCode.errorStatus) {
                ErrorStatus.NOT_FOUND -> HttpStatus.NOT_FOUND
                ErrorStatus.CONFLICT -> HttpStatus.CONFLICT
                ErrorStatus.INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
                ErrorStatus.ALREADY_EXISTS,
                ErrorStatus.BAD_REQUEST -> HttpStatus.BAD_REQUEST
                ErrorStatus.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED
            }

            ResponseEntity.status(httpStatus)
                .body(ErrorResponse<Nothing>(errorCode.code, errorCode.message))
        } else {
            model.addAttribute("message", errorCode.message)
            model.addAttribute("url", url)
            "error/alert"
        }
    }
}
