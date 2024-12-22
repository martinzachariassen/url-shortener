package no.zachen.urlshortener.controller

import jakarta.validation.Valid
import no.zachen.urlshortener.dto.ShortenUrlRequest
import no.zachen.urlshortener.model.UrlMapping
import no.zachen.urlshortener.service.UrlShortenerService
import no.zachen.urlshortener.utils.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UrlShortenerController(
    @Autowired private val urlShortenerService: UrlShortenerService,
) {
    val logger = logger()

    @PostMapping("/shorten")
    suspend fun shortenUrl(
        @Valid
        @RequestBody request: ShortenUrlRequest,
    ): ResponseEntity<UrlMapping> {
        logger.info("Receiving request to shorten url ${request.originalUrl}")
        val shortUrl = urlShortenerService.shortenUrl(request.originalUrl)
        logger.info("Created short url ${shortUrl.shortUrl} for original url ${shortUrl.originalUrl}")
        return ResponseEntity.ok(shortUrl)
    }

    @GetMapping("/{shortUrl}")
    suspend fun redirectToOriginalUrl(
        @PathVariable shortUrl: String,
    ): ResponseEntity<Unit> {
        logger.info("Receiving request to redirect to original url for short url $shortUrl")
        val originalUrl = urlShortenerService.getOriginalUrl(shortUrl)
        logger.info("Redirecting to original url $originalUrl")
        return ResponseEntity
            .status(302) // 302 Found (Redirect)
            .header("Location", originalUrl)
            .build()
    }
}
