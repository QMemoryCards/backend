package ru.spbstu.memory.cards.persistence

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import ru.spbstu.memory.cards.persistence.mapper.toUser
import ru.spbstu.memory.cards.persistence.mapper.toUserInsert
import ru.spbstu.memory.cards.persistence.model.User
import ru.spbstu.memory.cards.persistence.table.UserTable
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class UserRepository {
    fun findById(id: UUID): User? =
        transaction {
            UserTable
                .selectAll()
                .where { UserTable.id eq id }
                .singleOrNull()
                ?.toUser()
        }

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

    fun existsByEmail(email: String): Boolean =
        transaction {
            UserTable
                .selectAll()
                .where { UserTable.email eq email }
                .empty()
                .not()
        }

    fun existsByEmailAndId(
        email: String,
        id: UUID,
    ): Boolean =
        transaction {
            UserTable
                .selectAll()
                .where { (UserTable.email eq email) and (UserTable.id neq id) }
                .empty()
        }

    fun existsByLoginAndId(
        login: String,
        id: UUID,
    ): Boolean =
        transaction {
            UserTable
                .selectAll()
                .where { (UserTable.login eq login) and (UserTable.id neq id) }
                .empty()
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

    fun update(user: User): User =
        transaction {
            val now = OffsetDateTime.now()
            UserTable.update({ UserTable.id eq user.id }) {
                it[email] = user.email
                it[login] = user.login
                it[updatedAt] = now
            }
            user.copy(updatedAt = now)
        }

    fun updatePassword(
        id: UUID,
        newHash: String,
    ) {
        transaction {
            UserTable.update({ UserTable.id eq id }) {
                it[password] = newHash
                it[updatedAt] = OffsetDateTime.now()
            }
        }
    }

    fun delete(id: UUID) {
        transaction {
            UserTable.deleteWhere { UserTable.id eq id }
        }
    }
}
