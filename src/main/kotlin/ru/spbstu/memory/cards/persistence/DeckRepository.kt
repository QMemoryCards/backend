package ru.spbstu.memory.cards.persistence

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import ru.spbstu.memory.cards.persistence.mapper.toDeck
import ru.spbstu.memory.cards.persistence.mapper.toDeckInsert
import ru.spbstu.memory.cards.persistence.model.Deck
import ru.spbstu.memory.cards.persistence.table.DeckTable
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class DeckRepository {
    fun findById(id: UUID): Deck? =
        transaction {
            DeckTable
                .selectAll()
                .where { DeckTable.id eq id }
                .limit(1)
                .firstOrNull()
                ?.toDeck()
        }

    fun findAllByUserId(userId: UUID): List<Deck> =
        transaction {
            DeckTable
                .selectAll()
                .where { DeckTable.userId eq userId }
                .map { it.toDeck() }
        }

    fun saveNew(
        userId: UUID,
        name: String,
        description: String?,
    ): Deck {
        val now = OffsetDateTime.now()
        val id = UUID.randomUUID()

        return transaction {
            DeckTable.insert { stmt ->
                stmt.toDeckInsert(
                    id = id,
                    userId = userId,
                    name = name,
                    description = description,
                    learnedPercent = 0,
                    cardsCount = 0,
                    lastStudied = null,
                    createdAt = now,
                    updatedAt = now,
                )
            }

            Deck(
                id = id,
                userId = userId,
                name = name,
                description = description,
                learnedPercent = 0,
                cardsCount = 0,
                lastStudied = null,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
