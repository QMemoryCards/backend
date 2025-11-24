package ru.spbstu.memory.cards.persistence.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object CardTable : Table("cards") {
    val id = uuid("id")
    val deckId = reference("deck_id", DeckTable.id, onDelete = ReferenceOption.CASCADE)
    val question = varchar("question", length = 200)
    val answer = varchar("answer", length = 200)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
    val isLearned = bool("is_learned").default(false)

    override val primaryKey = PrimaryKey(id)
}
