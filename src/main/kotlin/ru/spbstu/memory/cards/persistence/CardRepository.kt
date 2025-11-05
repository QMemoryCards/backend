package ru.spbstu.memory.cards.persistence

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import ru.spbstu.memory.cards.persistence.mapper.toCard
import ru.spbstu.memory.cards.persistence.mapper.toCardInsert
import ru.spbstu.memory.cards.persistence.model.Card
import ru.spbstu.memory.cards.persistence.table.CardTable
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class CardRepository {
    fun findById(id: UUID): Card? =
        transaction {
            CardTable
                .selectAll()
                .where { CardTable.id eq id }
                .limit(1)
                .firstOrNull()
                ?.toCard()
        }

    fun findAllByDeckId(deckId: UUID): List<Card> =
        transaction {
            CardTable
                .selectAll()
                .where { CardTable.deckId eq deckId }
                .map { it.toCard() }
        }

    fun saveNew(
        deckId: UUID,
        question: String,
        answer: String,
    ): Card {
        val now = OffsetDateTime.now()
        val id = UUID.randomUUID()

        return transaction {
            CardTable.insert { stmt ->
                stmt.toCardInsert(
                    id = id,
                    deckId = deckId,
                    question = question,
                    answer = answer,
                    createdAt = now,
                    updatedAt = now,
                )
            }

            Card(
                id = id,
                deckId = deckId,
                question = question,
                answer = answer,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
