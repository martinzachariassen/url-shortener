package no.zachen.urlshortener.integration.setup

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import no.zachen.urlshortener.config.TestConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.io.IOException

/**
 * Provides a utility for setting up and managing a PostgreSQL database for integration testing.
 * This setup uses Testcontainers to start and manage a PostgreSQL container, performs schema
 * initialization with a specified SQL script, and exposes methods to start and stop the container.
 */
@SpringBootTest
@Component
class PostgresTestSetup(
    @Autowired private val testConfig: TestConfig,
) {
    private val logger: Logger = LoggerFactory.getLogger(PostgresTestSetup::class.java)
    private val postgresConfig = testConfig.postgres

    /**
     * A PostgreSQL container instance used for running and testing database operations in an isolated environment.
     *
     * This container is configured with the specified database name, username, and password. The PostgreSQL port
     * is dynamically mapped from the container's internal port to a host port that is automatically allocated.
     *
     * The container provides a reusable, controlled PostgreSQL environment for integration tests or other scenarios that
     * require a temporary database instance. It is initialized with custom port bindings by modifying the underlying
     * container configuration during creation.
     */
    private val postgresContainer =
        PostgreSQLContainer<Nothing>(DockerImageName.parse(postgresConfig.image)).apply {
            withDatabaseName(postgresConfig.database)
            withUsername(postgresConfig.username)
            withPassword(postgresConfig.password)

            // Bind internal PostgreSQL port to a dynamically allocated host port
            withCreateContainerCmdModifier { cmd ->
                cmd.withHostConfig(
                    HostConfig().withPortBindings(
                        PortBinding(Ports.Binding.bindPort(postgresConfig.port), ExposedPort(postgresConfig.port)),
                    ),
                )
            }
        }

    /**
     * Starts a PostgreSQL container in a background thread and initializes the database schema.
     *
     * This method ensures the PostgreSQL container is running before performing any database operations. If the container
     * is already running, it skips the startup process. In the case of a successful start, it logs the mapped external
     * port of the container. It also initializes the database schema after starting the container by invoking the
     * `initializeDatabaseSchema` function.
     *
     * The container startup process and subsequent operations are executed within the `Dispatchers.IO` context.
     *
     * @throws Exception If the PostgreSQL container fails to start or the database schema initialization encounters an error.
     */
    suspend fun startContainer() {
        withContext(Dispatchers.IO) {
            if (!postgresContainer.isRunning) {
                try {
                    postgresContainer.start()
                    logger.info("PostgreSQL container started on port: ${postgresContainer.getMappedPort(postgresConfig.port)}")
                } catch (ex: Exception) {
                    logger.error("Failed to start PostgreSQL container", ex)
                    throw ex
                }
            }

            initializeDatabaseSchema()
        }
    }

    /**
     * Initializes the database schema by executing an SQL script.
     *
     * This method connects to a PostgreSQL database using a dynamically mapped port from a running container.
     * It reads an SQL script from the specified path and executes the script to set up or update the database schema.
     * Logs informational messages for successful operations and errors when exceptions occur.
     *
     * @throws IOException If there is an error reading the SQL script.
     * @throws Exception If there is an error during SQL execution or database connection.
     */
    private suspend fun initializeDatabaseSchema() {
        // Build a DatabaseClient to interact with the PostgreSQL database
        val databaseClient =
            DatabaseClient
                .builder()
                .connectionFactory(
                    io.r2dbc.postgresql.PostgresqlConnectionFactory(
                        io.r2dbc.postgresql.PostgresqlConnectionConfiguration
                            .builder()
                            .host("localhost")
                            .port(postgresContainer.getMappedPort(5432)) // Use dynamically mapped port
                            .database(postgresConfig.database)
                            .username(postgresConfig.username)
                            .password(postgresConfig.password)
                            .build(),
                    ),
                ).build()

        val sql =
            try {
                readSqlScript(postgresConfig.sqlScriptPath)
            } catch (ex: IOException) {
                logger.error("Failed to read SQL script from file: ${postgresConfig.sqlScriptPath}", ex)
                throw ex
            }

        try {
            databaseClient
                .sql(sql)
                .fetch()
                .rowsUpdated()
                .awaitSingleOrNull()
            logger.info("Database schema initialized successfully!")
        } catch (ex: Exception) {
            logger.error("Failed to initialize database schema", ex)
            throw ex
        }
    }

    /**
     * Reads the content of an SQL script file from the provided path.
     *
     * This method reads the entire content of the file located at the given path
     * and returns it as a string. Logs an informational message upon successfully
     * reading the file.
     *
     * @param path The file path of the SQL script to be read.
     * @return The content of the SQL script as a string.
     * @throws IOException If an error occurs while reading the file.
     */
    private fun readSqlScript(path: String): String =
        File(path).readText().also {
            logger.info("Successfully read SQL script from file: $path")
        }

    /**
     * Stops a running PostgreSQL container in a background thread.
     *
     * This method checks if the PostgreSQL container is currently running. If it is,
     * it attempts to stop the container while logging success or failure messages.
     * The operation is executed within the `Dispatchers.IO` context to ensure non-blocking
     * behavior for IO-intensive tasks.
     *
     * Any exceptions during the stopping process are caught, logged as errors, and handled
     * gracefully to avoid abrupt failures in the application.
     */
    suspend fun stopContainer() {
        withContext(Dispatchers.IO) {
            if (postgresContainer.isRunning) {
                try {
                    postgresContainer.stop()
                    logger.info("PostgreSQL container stopped successfully.")
                } catch (ex: Exception) {
                    logger.error("Failed to stop PostgreSQL container", ex)
                }
            }
        }
    }
}
