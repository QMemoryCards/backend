package ru.spbstu.memory.cards.persistence

import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import ru.spbstu.memory.cards.persistence.mapper.toCard
import ru.spbstu.memory.cards.persistence.mapper.toCardInsert
import ru.spbstu.memory.cards.persistence.model.Card
import ru.spbstu.memory.cards.persistence.model.PaginatedResult
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

    fun findAllByDeckIdPaginated(
        deckId: UUID,
        page: Int,
        size: Int,
    ): PaginatedResult<Card> =
        transaction {
            val query = CardTable.selectAll().where { CardTable.deckId eq deckId }
            val totalElements = query.count()

            val cards =
                query
                    .orderBy(
                        CardTable.createdAt to SortOrder.ASC,
                        CardTable.id to SortOrder.ASC,
                    )
                    .limit(count = size)
                    .offset(start = (page * size).toLong())
                    .map { it.toCard() }

            PaginatedResult(cards, totalElements)
        }

    fun saveNew(
        deckId: UUID,
        question: String,
        answer: String,
    ): Card =
        transaction {
            val now = OffsetDateTime.now()
            val id = UUID.randomUUID()
            CardTable.insert {
                it.toCardInsert(
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

    fun update(card: Card): Card =
        transaction {
            val now = OffsetDateTime.now()
            CardTable.update({ CardTable.id eq card.id }) {
                it[question] = card.question
                it[answer] = card.answer
                it[updatedAt] = now
            }
            card.copy(updatedAt = now)
        }

    fun delete(id: UUID) {
        transaction {
            CardTable.deleteWhere { CardTable.id eq id }
        }
    }
}
