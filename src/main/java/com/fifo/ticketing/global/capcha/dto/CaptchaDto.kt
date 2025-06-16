package com.fifo.ticketing.global.capcha.dto

data class CaptchaResponse(
    val captchaId: String,
    val image: String
)

data class CaptchaVerifyRequest(
    val captchaId: String,
    val input: String
)