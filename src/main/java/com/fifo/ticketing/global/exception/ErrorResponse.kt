package com.fifo.ticketing.global.exception

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder("code", "message", "errors")
data class ErrorResponse<T>(
    val code: String,
    val message: String,

    @JsonInclude(Include.NON_EMPTY)
    val errors: T? = null
)
