package no.zachen.urlshortener.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for loading properties related to the test environment.
 *
 * This class is annotated with @Configuration and @ConfigurationProperties to bind
 * configuration values defined in a properties or YAML file (e.g., application.yml, test.yml)
 * to its fields.
 *
 * Properties are expected to have the prefix "test", and they will be mapped to the fields
 * of this class and its nested class `PostgresConfig`.
 */
@Configuration
@ConfigurationProperties(prefix = "test")
data class TestConfig(
    var postgres: PostgresConfig = PostgresConfig(), // Default instance of PostgresConfig to ensure initialization.
) {
    data class PostgresConfig(
        var image: String = "",
        var database: String = "",
        var username: String = "",
        var password: String = "",
        var port: Int = 5432,
        var sqlScriptPath: String = "",
    )
}
