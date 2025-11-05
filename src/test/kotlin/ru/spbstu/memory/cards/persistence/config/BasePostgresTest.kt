package ru.spbstu.memory.cards.persistence.config

import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Testcontainers
import ru.spbstu.memory.cards.persistence.table.CardTable
import ru.spbstu.memory.cards.persistence.table.DeckTable
import ru.spbstu.memory.cards.persistence.table.UserTable

@Testcontainers
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BasePostgresTest {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerDataSource(registry: DynamicPropertyRegistry) {
            val pg = PostgresTestContainer.instance
            registry.add("spring.datasource.url") { pg.jdbcUrl }
            registry.add("spring.datasource.username") { pg.username }
            registry.add("spring.datasource.password") { pg.password }
            registry.add("spring.datasource.driver-class-name") { pg.driverClassName }
            registry.add("spring.liquibase.enabled") { true }

            registry.add("spring.datasource.hikari.maximum-pool-size") { 5 }
            registry.add("spring.datasource.hikari.minimum-idle") { 1 }
            registry.add("spring.datasource.hikari.connection-timeout") { 30000 }
            registry.add("spring.datasource.hikari.idle-timeout") { 600000 }
            registry.add("spring.datasource.hikari.max-lifetime") { 1800000 }
        }
    }

    @BeforeEach
    fun cleanDatabase() {
        transaction {
            CardTable.deleteAll()
            DeckTable.deleteAll()
            UserTable.deleteAll()
        }
    }
}
