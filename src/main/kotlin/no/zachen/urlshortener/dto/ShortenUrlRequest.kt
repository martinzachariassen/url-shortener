package no.zachen.urlshortener.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ShortenUrlRequest(
    @field:NotBlank(message = "URL cannot be blank")
    @field:Pattern(
        regexp = "^(https?://)[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}.*\$",
        message = "Invalid URL format",
    )
    @field:Size(max = 2048, message = "URL length must not exceed 2048 characters")
    val originalUrl: String,
)
