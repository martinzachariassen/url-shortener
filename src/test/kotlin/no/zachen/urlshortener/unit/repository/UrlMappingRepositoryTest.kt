package no.zachen.urlshortener.unit.repository

import no.zachen.urlshortener.model.UrlMapping
import no.zachen.urlshortener.repository.UrlMappingRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class UrlMappingRepositoryTest {
    @Autowired
    private lateinit var urlMappingRepository: UrlMappingRepository

    @Test
    fun `should save and retrieve UrlMapping by shortUrl`() {
        val urlMapping =
            UrlMapping(
                originalUrl = "https://example.com",
                shortUrl = "short123",
            )
        urlMappingRepository.save(urlMapping)

        val retrieved = urlMappingRepository.findByShortUrl("short123")

        assertNotNull(retrieved)
        assertEquals("https://example.com", retrieved?.originalUrl)
        assertEquals("short123", retrieved?.shortUrl)
    }

    @Test
    fun `should return true if shortUrl exists`() {
        val urlMapping =
            UrlMapping(
                originalUrl = "https://example.com",
                shortUrl = "exists123",
            )
        urlMappingRepository.save(urlMapping)

        val exists = urlMappingRepository.existsByShortUrl("exists123")

        assertTrue(exists)
    }

    @Test
    fun `should return false if shortUrl does not exist`() {
        val exists = urlMappingRepository.existsByShortUrl("nonexistent")
        assertFalse(exists)
    }

    @Test
    fun `should return null when shortUrl is not found`() {
        val retrieved = urlMappingRepository.findByShortUrl("nonexistent")
        assertNull(retrieved)
    }
}
