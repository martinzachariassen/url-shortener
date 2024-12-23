package no.zachen.urlshortener.unit.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.zachen.urlshortener.model.UrlMapping
import no.zachen.urlshortener.repository.UrlMappingRepository
import no.zachen.urlshortener.service.UrlShortenerService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UrlShortenerServiceTest {
    private val repository: UrlMappingRepository = mockk()
    private val service = UrlShortenerService(repository)

    @Test
    fun `shortenUrl should generate unique short URLs and save to repository`() =
        runTest {
            val originalUrl = "https://example.com"
            coEvery { repository.existsByShortUrl(any()) } returns false
            coEvery { repository.save(any()) } answers { firstArg() }

            val urlMapping = service.shortenUrl(originalUrl)

            assertEquals(originalUrl, urlMapping.originalUrl)
            assertEquals(6, urlMapping.shortUrl.length)

            coVerify { repository.existsByShortUrl(urlMapping.shortUrl) }
            coVerify { repository.save(urlMapping) }
        }

    @Test
    fun `getOriginalUrl should return the original URL`() =
        runTest {
            coEvery { repository.findByShortUrl("foobar") } returns UrlMapping(shortUrl = "foobar", originalUrl = "https://example.com")

            val urlMapping = service.getOriginalUrl("foobar")

            assertEquals("https://example.com", urlMapping)
        }
}
