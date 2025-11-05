package ru.spbstu.memory.cards.persistence.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object DeckTable : Table("decks") {
    val id = uuid("id")
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", length = 90)
    val description = varchar("description", length = 200).nullable()
    val learnedPercent = integer("learned_percent").default(0)
    val cardsCount = integer("cards_count").default(0)
    val lastStudied = timestampWithTimeZone("last_studied").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}
