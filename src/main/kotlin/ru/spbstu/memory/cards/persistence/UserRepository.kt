package ru.spbstu.memory.cards.persistence

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import ru.spbstu.memory.cards.persistence.mapper.toUser
import ru.spbstu.memory.cards.persistence.mapper.toUserInsert
import ru.spbstu.memory.cards.persistence.model.User
import ru.spbstu.memory.cards.persistence.table.UserTable
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class UserRepository {
    fun findByLogin(login: String): User? =
        transaction {
            UserTable
                .selectAll()
                .where { UserTable.login eq login }
                .limit(1)
                .firstOrNull()
                ?.toUser()
        }

    fun existsByLogin(login: String): Boolean =
        transaction {
            UserTable
                .selectAll()
                .where { UserTable.login eq login }
                .empty()
                .not()
        }

    fun saveNew(
        email: String,
        login: String,
        passwordHash: String,
    ): User =
        transaction {
            val now = OffsetDateTime.now()
            val id = UUID.randomUUID()
            UserTable.insert {
                it.toUserInsert(
                    id = id,
                    email = email,
                    login = login,
                    passwordHash = passwordHash,
                    createdAt = now,
                    updatedAt = now,
                )
            }

            User(
                id = id,
                email = email,
                login = login,
                passwordHash = passwordHash,
                createdAt = now,
                updatedAt = now,
            )
        }
}
