package no.zachen.urlshortener.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "url_mapping")
data class UrlMapping(
    @Id
    @Column("id")
    val id: Int? = null,
    @Column("short_url")
    val shortUrl: String,
    @Column("original_url")
    val originalUrl: String,
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
