package no.zachen.urlshortener.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "url_mappings",
    uniqueConstraints = [UniqueConstraint(columnNames = ["shortUrl"])],
)
data class UrlMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID = UUID.randomUUID(),
    // Note: this is only the random part of the created url (not the whole thing, with https://..)
    @Column(name = "short_url", unique = true, nullable = false)
    val shortUrl: String,
    @Column(name = "original_url", nullable = false)
    val originalUrl: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
