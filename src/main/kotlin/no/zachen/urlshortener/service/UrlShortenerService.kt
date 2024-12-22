package no.zachen.urlshortener.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.zachen.urlshortener.model.UrlMapping
import no.zachen.urlshortener.repository.UrlMappingRepository
import no.zachen.urlshortener.utils.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface IUrlShortenerService {
    suspend fun shortenUrl(originalUrl: String): UrlMapping

    fun getOriginalUrl(shortUrl: String): String
}

@Service
class UrlShortenerService(
    @Autowired private val repository: UrlMappingRepository,
) : IUrlShortenerService {
    val logger = logger()

    override suspend fun shortenUrl(originalUrl: String): UrlMapping {
        val shortUrl = generateUniqueShortUrl()

        logger.info("Creating mapping for $shortUrl -> $originalUrl")

        val urlMapping =
            UrlMapping(
                shortUrl = shortUrl,
                originalUrl = originalUrl,
            )

        return withContext(Dispatchers.IO) {
            repository.save(urlMapping) // Save to the database asynchronously
        }
    }

    override fun getOriginalUrl(shortUrl: String): String = repository.findByShortUrl(shortUrl).originalUrl

    fun generateShortUrl(shortUrlLength: Int = 6): String =
        (1..shortUrlLength)
            .map { characters.random() }
            .joinToString("")

    private val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    private suspend fun generateUniqueShortUrl(): String {
        while (true) {
            val shortUrl = generateShortUrl()
            if (!withContext(Dispatchers.IO) { repository.existsByShortUrl(shortUrl) }) {
                logger.info("Generated unique shortUrl: $shortUrl")
                return shortUrl
            }
            logger.info("ShortUrl $shortUrl already exists. Generating a new one...")
        }
    }
}
