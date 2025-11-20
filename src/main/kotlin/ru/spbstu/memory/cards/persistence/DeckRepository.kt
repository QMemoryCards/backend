package ru.spbstu.memory.cards.persistence

import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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

    fun findAllByUserId(
        userId: UUID,
        page: Int,
        size: Int,
    ): Pair<List<Deck>, Long> =
        transaction {
            val query = DeckTable.selectAll().where { DeckTable.userId eq userId }
            val totalElements = query.count()
            val decks =
                query
                    .orderBy(DeckTable.createdAt to SortOrder.DESC)
                    .limit(count = size)
                    .offset(start = (page * size).toLong())
                    .map { it.toDeck() }
            decks to totalElements
        }

    fun countByUserId(userId: UUID): Long =
        transaction {
            DeckTable
                .selectAll()
                .where { DeckTable.userId eq userId }
                .count()
        }

    fun existsByUserIdAndName(
        userId: UUID,
        name: String,
    ): Boolean =
        transaction {
            DeckTable
                .selectAll()
                .where { (DeckTable.userId eq userId) and (DeckTable.name eq name) }
                .empty()
                .not()
        }

    fun existsByUserIdAndNameAndId(
        userId: UUID,
        name: String,
        excludeDeckId: UUID,
    ): Boolean =
        transaction {
            DeckTable
                .selectAll()
                .where {
                    (DeckTable.userId eq userId) and
                        (DeckTable.name eq name) and
                        (DeckTable.id neq excludeDeckId)
                }
                .empty()
        }

    fun existsById(id: UUID): Boolean =
        transaction {
            DeckTable
                .selectAll()
                .where { DeckTable.id eq id }
                .empty()
                .not()
        }

    fun saveNew(
        userId: UUID,
        name: String,
        description: String?,
    ): Deck =
        transaction {
            val now = OffsetDateTime.now()
            val id = UUID.randomUUID()
            DeckTable.insert {
                it.toDeckInsert(
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

    fun update(deck: Deck): Deck =
        transaction {
            val now = OffsetDateTime.now()
            DeckTable.update({ DeckTable.id eq deck.id }) {
                it[name] = deck.name
                it[description] = deck.description
                it[cardsCount] = deck.cardsCount
                it[updatedAt] = now
            }
            deck.copy(updatedAt = now)
        }

    fun delete(id: UUID) {
        transaction {
            DeckTable.deleteWhere { DeckTable.id eq id }
        }
    }
}
