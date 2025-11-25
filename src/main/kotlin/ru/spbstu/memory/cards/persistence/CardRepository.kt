package ru.spbstu.memory.cards.persistence

import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
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
                    .orderBy(CardTable.createdAt to SortOrder.ASC)
                    .limit(count = size)
                    .offset(start = (page * size).toLong())
                    .map { it.toCard() }

            PaginatedResult(cards, totalElements)
        }

    fun findAllByDeckId(deckId: UUID): List<Card> =
        transaction {
            CardTable.selectAll()
                .where { CardTable.deckId eq deckId }
                .orderBy(CardTable.createdAt to SortOrder.ASC)
                .map { it.toCard() }
        }

    fun getLearnedCount(deckId: UUID): Long =
        transaction {
            CardTable.selectAll()
                .where { (CardTable.deckId eq deckId) and (CardTable.isLearned eq true) }
                .count()
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

    fun copyAllToDeck(
        sourceDeckId: UUID,
        targetDeckId: UUID,
    ): Int {
        var inserted = 0
        transaction {
            val cardsToCopy =
                CardTable.selectAll()
                    .where { CardTable.deckId eq sourceDeckId }
                    .map { it.toCard() }

            cardsToCopy.forEach { card ->
                val now = OffsetDateTime.now()
                val newId = UUID.randomUUID()
                inserted +=
                    CardTable.insert {
                        it.toCardInsert(
                            id = newId,
                            deckId = targetDeckId,
                            question = card.question,
                            answer = card.answer,
                            createdAt = now,
                            updatedAt = now,
                        )
                    }.insertedCount
            }
        }

        return inserted
    }

    fun update(card: Card): Card =
        transaction {
            val now = OffsetDateTime.now()
            CardTable.update({ CardTable.id eq card.id }) {
                it[question] = card.question
                it[answer] = card.answer
                it[updatedAt] = now
                it[isLearned] = card.isLearned
            }
            card.copy(updatedAt = now)
        }

    fun delete(id: UUID) {
        transaction {
            CardTable.deleteWhere { CardTable.id eq id }
        }
    }
}
