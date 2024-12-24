package no.zachen.urlshortener.integration.repository

import kotlinx.coroutines.test.runTest
import no.zachen.urlshortener.integration.setup.PostgresTestSetup
import no.zachen.urlshortener.model.UrlMapping
import no.zachen.urlshortener.repository.UrlMappingRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
class UrlMappingRepositoryIntegrationTest {
    @Autowired
    private lateinit var urlMappingRepository: UrlMappingRepository

    @BeforeEach
    fun setup() =
        runTest {
            urlMappingRepository.deleteAll()
        }

    @AfterEach
    fun teardown() =
        runTest {
            urlMappingRepository.deleteAll()
        }

    @Test
    fun `should connect to the database and perform CRUD operations`() =
        runTest {
            // Create
            val saved =
                urlMappingRepository.save(
                    UrlMapping(shortUrl = "test", originalUrl = "https://test.com"),
                )
            Assertions.assertNotNull(saved.id)

            // Read
            val retrieved = urlMappingRepository.findByShortUrl("test")
            Assertions.assertNotNull(retrieved)
            Assertions.assertEquals("https://test.com", retrieved?.originalUrl)

            // Update
            val updated =
                urlMappingRepository.save(
                    saved.copy(originalUrl = "https://updated.com"),
                )
            val updatedRetrieved = urlMappingRepository.findByShortUrl("test")
            Assertions.assertNotNull(updatedRetrieved)
            Assertions.assertEquals("https://updated.com", updatedRetrieved?.originalUrl)

            // Delete
            urlMappingRepository.delete(updated)
            val afterDeletion = urlMappingRepository.findByShortUrl("test")
            Assertions.assertNull(afterDeletion)
        }

    @Test
    fun `should return null for non-existent short URL`() =
        runTest {
            val retrieved = urlMappingRepository.findByShortUrl("nonexistent")
            Assertions.assertNull(retrieved)
        }

    @Test
    fun `should verify existence of short URL`() =
        runTest {
            // Create
            urlMappingRepository.save(
                UrlMapping(shortUrl = "existsTest", originalUrl = "https://exists.com"),
            )

            // Verify existence
            val exists = urlMappingRepository.existsByShortUrl("existsTest")
            Assertions.assertTrue(exists)

            val doesNotExist = urlMappingRepository.existsByShortUrl("doesNotExist")
            Assertions.assertFalse(doesNotExist)
        }

    @Test
    fun `should handle multiple records correctly`() =
        runTest {
            // Create multiple records
            urlMappingRepository.save(UrlMapping(shortUrl = "multi1", originalUrl = "https://multi1.com"))
            urlMappingRepository.save(UrlMapping(shortUrl = "multi2", originalUrl = "https://multi2.com"))

            // Verify retrieval
            val first = urlMappingRepository.findByShortUrl("multi1")
            val second = urlMappingRepository.findByShortUrl("multi2")

            Assertions.assertNotNull(first)
            Assertions.assertEquals("https://multi1.com", first?.originalUrl)

            Assertions.assertNotNull(second)
            Assertions.assertEquals("https://multi2.com", second?.originalUrl)
        }

    companion object {
        @JvmStatic
        @AfterAll
        fun stopContainer(): Unit =
            runTest {
                PostgresTestSetup.stopContainer()
            }

        @JvmStatic
        @BeforeAll
        fun initializeContainer(): Unit =
            runTest {
                PostgresTestSetup.startContainer() // Await container startup
                println("PostgreSQL Testcontainer initialized:")
                println("R2DBC URL: ${PostgresTestSetup.getR2dbcUrl()}")
            }
    }
}
