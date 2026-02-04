package ru.spbstu.memory.cards.config

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import jakarta.servlet.FilterChain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import ru.spbstu.memory.cards.config.logging.RequestResponseLoggingFilter
import java.nio.charset.StandardCharsets

class RequestResponseLoggingFilterTest {
    private val filter = RequestResponseLoggingFilter()

    private val logger: Logger =
        LoggerFactory.getLogger(RequestResponseLoggingFilter::class.java) as Logger

    private val originalLevel: Level? = logger.level

    @AfterEach
    fun tearDown() {
        logger.level = originalLevel
    }

    @Test
    fun doFilter_shouldLogBodies_whenNotSensitive_andInfoEnabled_andBodiesNotEmpty() {
        logger.level = Level.INFO

        val request =
            MockHttpServletRequest("POST", "/api/v1/decks").apply {
                contentType = "application/json"
                setContent("""{"a":1}""".toByteArray(StandardCharsets.UTF_8))
            }

        val response = MockHttpServletResponse()

        val chain =
            FilterChain { req, res ->
                req.inputStream.readBytes()
                res.writer.write("""{"ok":true}""")
            }

        filter.doFilter(request, response, chain)

        assertThat(response.contentAsString).isEqualTo("""{"ok":true}""")
    }

    @Test
    fun doFilter_shouldMaskBody_whenSensitivePath() {
        logger.level = Level.INFO

        val request =
            MockHttpServletRequest("POST", "/api/v1/auth/login").apply {
                setContent("""{"login":"u","password":"p"}""".toByteArray(StandardCharsets.UTF_8))
            }

        val response = MockHttpServletResponse()

        val chain =
            FilterChain { req, res ->
                req.inputStream.readBytes()
                res.writer.write("""{"token":"x"}""")
            }

        filter.doFilter(request, response, chain)

        assertThat(response.contentAsString).isEqualTo("""{"token":"x"}""")
    }

    @Test
    fun doFilter_shouldNotLog_whenInfoDisabled() {
        logger.level = Level.ERROR

        val request =
            MockHttpServletRequest("POST", "/api/v1/decks").apply {
                setContent("""{"a":1}""".toByteArray(StandardCharsets.UTF_8))
                characterEncoding = "UTF-16"
            }

        val response = MockHttpServletResponse()

        val chain =
            FilterChain { req, res ->
                req.inputStream.readBytes()
                res.writer.write("ok")
            }

        filter.doFilter(request, response, chain)

        assertThat(response.contentAsString).isEqualTo("ok")
    }
}
