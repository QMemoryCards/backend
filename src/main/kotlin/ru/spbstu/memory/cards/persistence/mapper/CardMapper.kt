package ru.spbstu.memory.cards.persistence.mapper

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import ru.spbstu.memory.cards.persistence.model.Card
import ru.spbstu.memory.cards.persistence.table.CardTable
import java.time.OffsetDateTime
import java.util.UUID

fun ResultRow.toCard(): Card =
    Card(
        id = this[CardTable.id],
        deckId = this[CardTable.deckId],
        question = this[CardTable.question],
        answer = this[CardTable.answer],
        createdAt = this[CardTable.createdAt],
        updatedAt = this[CardTable.updatedAt],
        isLearned = this[CardTable.isLearned],
    )

fun InsertStatement<Number>.toCardInsert(
    id: UUID,
    deckId: UUID,
    question: String,
    answer: String,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime,
) {
    this[CardTable.id] = id
    this[CardTable.deckId] = deckId
    this[CardTable.question] = question
    this[CardTable.answer] = answer
    this[CardTable.createdAt] = createdAt
    this[CardTable.updatedAt] = updatedAt
}
