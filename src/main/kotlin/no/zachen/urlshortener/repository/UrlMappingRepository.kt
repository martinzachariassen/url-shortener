package no.zachen.urlshortener.repository

import no.zachen.urlshortener.model.UrlMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UrlMappingRepository : JpaRepository<UrlMapping, UUID> {
    fun findByShortUrl(shortUrl: String): UrlMapping

    fun existsByShortUrl(shortUrl: String): Boolean
}
