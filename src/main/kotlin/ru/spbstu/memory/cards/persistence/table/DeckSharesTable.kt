package ru.spbstu.memory.cards.persistence.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object DeckSharesTable : Table("deck_shares") {
    val deckId = reference("deck_id", DeckTable.id, onDelete = ReferenceOption.CASCADE)
    val token = uuid("token")

    override val primaryKey = PrimaryKey(token)
}
