package com.fifo.ticketing.global.exception

import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error

object OAuth2ExceptionFactory {

    fun fromErrorCode(errorCode: ErrorCode): OAuth2AuthenticationException {
        return OAuth2AuthenticationException(
            OAuth2Error(errorCode.code, errorCode.message, null),
            errorCode.message
        )
    }
}
