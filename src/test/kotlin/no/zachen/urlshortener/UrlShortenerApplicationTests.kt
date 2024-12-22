package no.zachen.urlshortener

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import no.zachen.urlshortener.model.UrlMapping
import no.zachen.urlshortener.repository.UrlMappingRepository
import no.zachen.urlshortener.service.UrlShortenerService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.validation.Validator

@SpringBootTest
class UrlShortenerApplicationTests {
    private val repository: UrlMappingRepository = mockk()
    private val service = UrlShortenerService(repository)

    @Test
    fun `shortenUrl should generate unique short URLs and save to repository`() =
        runTest {
            // Arrange
            val originalUrl = "https://example.com" // Updated to match "originalUrl"

            // Mock repository behavior
            coEvery { repository.existsByShortUrl(any()) } returns false
            coEvery { repository.save(any()) } answers { firstArg() }

            // Act
            val urlMapping = service.shortenUrl(originalUrl)

            println("Url mapping: $urlMapping")

            // Assert
            assertEquals(originalUrl, urlMapping.originalUrl)
            assertEquals(6, urlMapping.shortUrl.length)

            coVerify { repository.existsByShortUrl(urlMapping.shortUrl) }
            coVerify { repository.save(urlMapping) }
        }

    @Test
    fun `should fetch a short url created`() =
        runTest {
            coEvery { repository.findByShortUrl(any()) } returns
                UrlMapping(
                    shortUrl = "foobar",
                    originalUrl = "https://example.com",
                )

            val urlMapping = service.getOriginalUrl("foobar")

            assertEquals("https://example.com", urlMapping)
        }

    @Test
    fun `should retry generating a short URL until a unique one is found`() =
        runTest {
            // Mock repository behavior
            coEvery { repository.existsByShortUrl("foobar") } returns true // First URL exists
            coEvery { repository.existsByShortUrl("uniqueUrl") } returns false // Second URL is unique
            coEvery { repository.save(any()) } answers { firstArg() } // Save the final mapping

            // Mock generateShortUrl to produce deterministic values
            mockkObject(service)
            every { service.generateShortUrl() } returnsMany listOf("foobar", "uniqueUrl")

            // Act
            val urlMapping = service.shortenUrl("https://example.com")

            // Assert
            assertNotEquals("foobar", urlMapping.shortUrl) // Ensure "foobar" was not used
            assertEquals("uniqueUrl", urlMapping.shortUrl) // Ensure "uniqueUrl" is used
            assertEquals("https://example.com", urlMapping.originalUrl)

            // Verify repository interactions
            coVerify { repository.existsByShortUrl("foobar") } // Checked "foobar" first
            coVerify { repository.existsByShortUrl("uniqueUrl") } // Checked "uniqueUrl" next
            coVerify { repository.save(any()) } // Saved the final mapping

            // Clear mocked object behavior after the test
            unmockkObject(service)
        }

    private val validator: Validator =
        org.springframework.validation.beanvalidation
            .LocalValidatorFactoryBean()

//    @Test
//    fun `should return error for invalid URL format`() {
//        val request = ShortenUrlRequest(originalUrl = "invalid-url")
//        val bindingResult = BeanPropertyBindingResult(request, "shortenUrlRequest")
//        validator.validate(request, bindingResult)
//
//        assertEquals(1, bindingResult.errorCount)
//        assertEquals("Invalid URL format", bindingResult.getFieldError("originalUrl")?.defaultMessage)
//    }
//
//    @Test
//    fun `should pass validation for valid URL`() {
//        val request = ShortenUrlRequest(originalUrl = "https://example.com")
//        val bindingResult = BeanPropertyBindingResult(request, "shortenUrlRequest")
//        validator.validate(request, bindingResult)
//
//        assertEquals(0, bindingResult.errorCount)
//    }
}
