package ru.spbstu.memory.cards.persistence.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object UserTable : Table("users") {
    val id = uuid("id")
    val email = varchar("email", length = 255).uniqueIndex()
    val login = varchar("login", length = 64).uniqueIndex()
    val password = varchar("password", length = 255)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
