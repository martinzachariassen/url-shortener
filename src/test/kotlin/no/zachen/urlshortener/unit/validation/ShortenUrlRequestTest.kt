package no.zachen.urlshortener.unit.validation

import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import no.zachen.urlshortener.dto.ShortenUrlRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class ShortenUrlRequestTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `should pass validation with valid URL`() {
        val request =
            ShortenUrlRequest(
                originalUrl = "https://example.com/path",
            )

        val violations = validator.validate(request)

        assertEquals(0, violations.size)
    }

    @Test
    fun `should fail validation when URL is blank`() {
        val request =
            ShortenUrlRequest(
                originalUrl = "   ", // Blank URL
            )

        val violations = validator.validate(request)

        assertEquals(2, violations.size)
        val messages = violations.map { it.message }

        assertTrue(messages.contains("URL cannot be blank"), "Expected 'URL cannot be blank' in violations")
        assertTrue(messages.contains("Invalid URL format"), "Expected 'Invalid URL format' in violations")
    }

    @Test
    fun `should fail validation with invalid URL format`() {
        val request =
            ShortenUrlRequest(
                originalUrl = "invalid-url",
            )

        val violations = validator.validate(request)

        assertEquals(1, violations.size)
        assertEquals("Invalid URL format", violations.first().message)
    }

    @Test
    fun `should fail validation when URL exceeds max length`() {
        val longUrl = "https://" + "a".repeat(2049) + ".com" // Exceeds 2048 characters
        val request =
            ShortenUrlRequest(
                originalUrl = longUrl,
            )

        val violations = validator.validate(request)
        println(violations)

        assertEquals(1, violations.size)
        assertEquals("URL length must not exceed 2048 characters", violations.first().message)
    }
}
