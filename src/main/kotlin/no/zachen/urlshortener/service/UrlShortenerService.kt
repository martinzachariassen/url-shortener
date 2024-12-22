package no.zachen.urlshortener.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.zachen.urlshortener.model.UrlMapping
import no.zachen.urlshortener.repository.UrlMappingRepository
import no.zachen.urlshortener.utils.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
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
            repository.save(urlMapping)
        }
    }

    @Cacheable("shortUrls")
    override fun getOriginalUrl(shortUrl: String): String {
        val urlMapping =
            repository.findByShortUrl(shortUrl)
                ?: throw NoSuchElementException("No mapping found for shortUrl: $shortUrl")
        return urlMapping.originalUrl
    }

    fun generateShortUrl(shortUrlLength: Int = 6): String {
        val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

        return (1..shortUrlLength)
            .map { characters.random() }
            .joinToString("")
    }

    private suspend fun generateUniqueShortUrl(): String {
        while (true) {
            val shortUrl = generateShortUrl()
            val exists = withContext(Dispatchers.IO) { repository.existsByShortUrl(shortUrl) }
            if (!exists) {
                logger.info("Generated unique shortUrl: $shortUrl")
                return shortUrl
            }
            logger.info("ShortUrl $shortUrl already exists. Retrying...")
        }
    }
}
