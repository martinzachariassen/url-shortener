package no.zachen.urlshortener.repository

import no.zachen.urlshortener.model.UrlMapping
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UrlMappingRepository : CoroutineCrudRepository<UrlMapping, Int> {
    @Query("SELECT * FROM url_mapping WHERE short_url = :shortUrl")
    suspend fun findByShortUrl(shortUrl: String): UrlMapping?

    @Query("SELECT EXISTS(SELECT 1 FROM url_mapping WHERE short_url = :shortUrl)")
    suspend fun existsByShortUrl(shortUrl: String): Boolean
}
