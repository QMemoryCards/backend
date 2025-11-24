package ru.spbstu.memory.cards.config.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

@Order(value = Ordered.HIGHEST_PRECEDENCE)
@Component
class RequestResponseLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(RequestResponseLoggingFilter::class.java)

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val cachingRequest = ContentCachingRequestWrapper(request)
        val cachingResponse = ContentCachingResponseWrapper(response)

        try {
            filterChain.doFilter(cachingRequest, cachingResponse)
        } finally {
            logIncoming(cachingRequest)
            logOutgoing(cachingResponse)

            cachingResponse.copyBodyToResponse()
        }
    }

    private fun logIncoming(request: ContentCachingRequestWrapper) {
        val method = request.method
        val uriWithQuery =
            buildString {
                append(request.requestURL)
            }
        val sensitive = isSensitive(request)

        val bodyString =
            if (!sensitive) {
                val bodyBytes = request.contentAsByteArray
                if (bodyBytes.isNotEmpty()) {
                    val charset: Charset =
                        request.characterEncoding
                            ?.let { Charset.forName(it) }
                            ?: StandardCharsets.UTF_8

                    String(bodyBytes, charset)
                } else {
                    ""
                }
            } else {
                "***"
            }

        if (log.isInfoEnabled) {
            log.info(
                """

                Incoming request:
                $method $uriWithQuery
                body: $bodyString

                """.trimIndent(),
            )
        }
    }

    private fun logOutgoing(response: ContentCachingResponseWrapper) {
        val status = response.status

        val bodyBytes = response.contentAsByteArray
        val bodyString =
            if (bodyBytes.isNotEmpty()) {
                val charset: Charset =
                    response.characterEncoding
                        ?.let { Charset.forName(it) }
                        ?: StandardCharsets.UTF_8

                String(bodyBytes, charset)
            } else {
                ""
            }

        if (log.isInfoEnabled) {
            log.info(
                """

                Outgoing response:
                $status
                body: $bodyString

                """.trimIndent(),
            )
        }
    }

    private fun isSensitive(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/api/v1/auth/login") ||
            path.startsWith("/api/v1/auth/register") ||
            path.startsWith("/api/v1/users/me/password")
    }
}
