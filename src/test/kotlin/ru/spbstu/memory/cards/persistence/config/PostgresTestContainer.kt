package ru.spbstu.memory.cards.persistence.config

import org.testcontainers.containers.PostgreSQLContainer

object PostgresTestContainer {
    val instance: PostgreSQLContainer<*> =
        PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("memory_cards_test")
            withUsername("test")
            withPassword("test")
            withReuse(true)
            start()
        }
}
