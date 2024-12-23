package no.zachen.urlshortener.unit.utils

import no.zachen.urlshortener.utils.logger
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.slf4j.Logger

class LoggerUtilsTest {
    @Test
    fun `logger should return a valid Logger instance`() {
        class TestClass
        val logger: Logger = TestClass().logger()

        assertNotNull(logger, "Logger should not be null")
    }
}
