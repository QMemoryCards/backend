package ru.spbstu.memory.cards.persistence.mapper

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import ru.spbstu.memory.cards.persistence.model.User
import ru.spbstu.memory.cards.persistence.table.UserTable
import java.time.OffsetDateTime
import java.util.UUID

fun ResultRow.toUser(): User =
    User(
        id = this[UserTable.id],
        email = this[UserTable.email],
        login = this[UserTable.login],
        passwordHash = this[UserTable.password],
        createdAt = this[UserTable.createdAt],
        updatedAt = this[UserTable.updatedAt],
    )

fun InsertStatement<Number>.toUserInsert(
    id: UUID,
    email: String,
    login: String,
    passwordHash: String,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime,
) {
    this[UserTable.id] = id
    this[UserTable.email] = email
    this[UserTable.login] = login
    this[UserTable.password] = passwordHash
    this[UserTable.createdAt] = createdAt
    this[UserTable.updatedAt] = updatedAt
}
