package ru.spbstu.memory.cards.persistence.mapper

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import ru.spbstu.memory.cards.persistence.model.DeckShare
import ru.spbstu.memory.cards.persistence.table.DeckSharesTable

fun ResultRow.toDeckShare(): DeckShare =
    DeckShare(
        deckId = this[DeckSharesTable.deckId],
        token = this[DeckSharesTable.token],
    )

fun InsertStatement<Number>.toDeckShareInsert(
    deckId: java.util.UUID,
    token: java.util.UUID,
) {
    this[DeckSharesTable.deckId] = deckId
    this[DeckSharesTable.token] = token
}
