package no.zachen.urlshortener.integration.setup

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.r2dbc.core.DatabaseClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

object PostgresTestSetup {
    private val postgresContainer =
        PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:latest")).apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
            // Forces the container’s internal port 5432 to bind to the host’s port 5432.
            // Overrides the default behavior of dynamically assigning a random host port.
            withCreateContainerCmdModifier { cmd ->
                // Replaces the default HostConfig of the container with a custom HostConfig that
                // includes your specific configuration (in this case, port bindings).
                cmd.withHostConfig(
                    // Creates a new HostConfig object and applies port bindings to it.
                    HostConfig().withPortBindings(PortBinding(Ports.Binding.bindPort(5432), ExposedPort(5432))),
                )
            }
        }

    suspend fun startContainer() {
        withContext(Dispatchers.IO) {
            if (!postgresContainer.isRunning) {
                postgresContainer.start()
            }

            println("PostgreSQL container started:")
            println("R2DBC URL: ${getR2dbcUrl()}")

            // Initialize the database schema
            initializeDatabase()
        }
    }

    private suspend fun initializeDatabase() {
        // Create a DatabaseClient using R2DBC
        val databaseClient =
            DatabaseClient
                .builder()
                .connectionFactory(
                    io.r2dbc.postgresql.PostgresqlConnectionFactory(
                        io.r2dbc.postgresql.PostgresqlConnectionConfiguration
                            .builder()
                            .host("localhost") // Host of your PostgreSQL container
                            .port(5432) // Port to which your container is mapped
                            .database("testdb")
                            .username("test")
                            .password("test")
                            .build(),
                    ),
                ).build()

        // Execute the SQL migration to set up the `url_mapping` table
        databaseClient
            .sql(
                """
        CREATE TABLE IF NOT EXISTS url_mapping (
            id SERIAL PRIMARY KEY,
            short_url VARCHAR(255) NOT NULL UNIQUE,
            original_url TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        
        CREATE UNIQUE INDEX idx_short_url_unique ON url_mapping(short_url);
        """,
            ).fetch()
            .rowsUpdated()
            .awaitSingle()

        println("Database schema initialized!")
    }

//    fun getJdbcUrl(): String = postgresContainer.jdbcUrl

    fun getR2dbcUrl(): String = "r2dbc:postgresql://${postgresContainer.host}:${postgresContainer.getMappedPort(5432)}/testdb"

//    fun getUsername(): String = postgresContainer.username

//    fun getPassword(): String = postgresContainer.password

    fun stopContainer() {
        if (postgresContainer.isRunning) {
            postgresContainer.stop()
        }
    }
}
