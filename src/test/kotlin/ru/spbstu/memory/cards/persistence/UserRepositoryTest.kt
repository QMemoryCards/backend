package ru.spbstu.memory.cards.persistence

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.spbstu.memory.cards.persistence.config.BasePostgresTest
import ru.spbstu.memory.cards.persistence.mapper.toUser
import ru.spbstu.memory.cards.persistence.table.UserTable

class UserRepositoryTest : BasePostgresTest() {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun findByLogin_shouldReturnUser_whenUserExists() {
        transaction {
            UserTable.insert {
                it[UserTable.id] = java.util.UUID.randomUUID()
                it[UserTable.email] = "test@example.com"
                it[UserTable.login] = "testuser"
                it[UserTable.password] = "hash"
                it[UserTable.createdAt] = java.time.OffsetDateTime.now()
                it[UserTable.updatedAt] = java.time.OffsetDateTime.now()
            }
        }

        val result = userRepository.findByLogin("testuser")

        assertThat(result).isNotNull
        assertThat(result!!.login).isEqualTo("testuser")
        assertThat(result.email).isEqualTo("test@example.com")
    }

    @Test
    fun findByLogin_shouldReturnNull_whenUserNotExists() {
        val result = userRepository.findByLogin("nonexistent")

        assertThat(result).isNull()
    }

    @Test
    fun existsByLogin_shouldReturnTrue_whenUserExists() {
        transaction {
            UserTable.insert {
                it[UserTable.id] = java.util.UUID.randomUUID()
                it[UserTable.email] = "exists@example.com"
                it[UserTable.login] = "existsuser"
                it[UserTable.password] = "hash"
                it[UserTable.createdAt] = java.time.OffsetDateTime.now()
                it[UserTable.updatedAt] = java.time.OffsetDateTime.now()
            }
        }

        val result = userRepository.existsByLogin("existsuser")

        assertThat(result).isTrue()
    }

    @Test
    fun existsByLogin_shouldReturnFalse_whenUserNotExists() {
        val result = userRepository.existsByLogin("nonexistent")

        assertThat(result).isFalse()
    }

    @Test
    fun saveNew_shouldInsertUser_whenValidData() {
        val email = "new@example.com"
        val login = "newuser"
        val passwordHash = "newhash"

        val result = userRepository.saveNew(email, login, passwordHash)

        val savedUser =
            transaction {
                UserTable
                    .selectAll()
                    .where { UserTable.id eq result.id }
                    .firstOrNull()
                    ?.toUser()
            }

        assertThat(savedUser).isNotNull
        assertThat(savedUser!!.email).isEqualTo(email)
        assertThat(savedUser.login).isEqualTo(login)
        assertThat(savedUser.passwordHash).isEqualTo(passwordHash)
        assertThat(savedUser.id).isEqualTo(result.id)
    }
}
