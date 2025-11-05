package ru.spbstu.memory.cards.persistence.mapper

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import ru.spbstu.memory.cards.persistence.model.Deck
import ru.spbstu.memory.cards.persistence.table.DeckTable
import java.time.OffsetDateTime
import java.util.UUID

fun ResultRow.toDeck(): Deck =
    Deck(
        id = this[DeckTable.id],
        userId = this[DeckTable.userId],
        name = this[DeckTable.name],
        description = this[DeckTable.description],
        learnedPercent = this[DeckTable.learnedPercent],
        cardsCount = this[DeckTable.cardsCount],
        lastStudied = this[DeckTable.lastStudied],
        createdAt = this[DeckTable.createdAt],
        updatedAt = this[DeckTable.updatedAt],
    )

fun InsertStatement<Number>.toDeckInsert(
    id: UUID,
    userId: UUID,
    name: String,
    description: String?,
    learnedPercent: Int,
    cardsCount: Int,
    lastStudied: OffsetDateTime?,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime,
) {
    this[DeckTable.id] = id
    this[DeckTable.userId] = userId
    this[DeckTable.name] = name
    this[DeckTable.description] = description
    this[DeckTable.learnedPercent] = learnedPercent
    this[DeckTable.cardsCount] = cardsCount
    this[DeckTable.lastStudied] = lastStudied
    this[DeckTable.createdAt] = createdAt
    this[DeckTable.updatedAt] = updatedAt
}
