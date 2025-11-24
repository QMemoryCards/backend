package ru.spbstu.memory.cards.config.auth

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebCorsConfig : WebMvcConfigurer {
    @Value("\${cors.originPatterns:http://localhost:*}")
    private val corsOriginPatterns: String = ""

    override fun addCorsMappings(registry: CorsRegistry) {
        val allowedOrigins = corsOriginPatterns.split(",").toTypedArray()
        LoggerFactory.getLogger(javaClass).info("Origins: ${allowedOrigins.contentToString()}")

        registry.addMapping("/**")
            .allowedOriginPatterns(*allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
