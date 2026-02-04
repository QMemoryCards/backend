package ru.spbstu.memory.cards.persistence

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import ru.spbstu.memory.cards.persistence.config.BasePostgresTest
import ru.spbstu.memory.cards.persistence.table.UserTable
import java.time.OffsetDateTime
import java.util.UUID

class UserRepositoryTest : BasePostgresTest() {
    @Test
    fun findById_shouldReturnUser_whenUserExists() {
        val userId = createTestUser()

        val result = userRepository.findById(userId)

        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(userId)
        assertThat(result.login).isEqualTo("testuser")
        assertThat(result.email).isEqualTo("test@example.com")
    }

    @Test
    fun findById_shouldReturnNull_whenUserNotExists() {
        val result = userRepository.findById(UUID.randomUUID())

        assertThat(result).isNull()
    }

    @Test
    fun findByLogin_shouldReturnUser_whenUserExists() {
        transaction {
            UserTable.insert {
                it[UserTable.id] = UUID.randomUUID()
                it[UserTable.email] = "test@example.com"
                it[UserTable.login] = "testuser"
                it[UserTable.password] = "hash"
                it[UserTable.createdAt] = OffsetDateTime.now()
                it[UserTable.updatedAt] = OffsetDateTime.now()
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
    fun existsByEmailAndId_shouldReturnFalse_whenAnotherUserHasEmail() {
        createTestUser(email = "same@example.com", login = "user1")
        val user2Id = createTestUser(email = "other@example.com", login = "user2")

        val result = userRepository.existsByEmailAndId("same@example.com", user2Id)

        assertThat(result).isFalse()
    }

    @Test
    fun existsByEmailAndId_shouldReturnTrue_whenOnlyThisUserHasEmail() {
        val userId = createTestUser(email = "only@example.com", login = "onlyuser")

        val result = userRepository.existsByEmailAndId("only@example.com", userId)

        assertThat(result).isTrue()
    }

    @Test
    fun saveNew_shouldInsertUser_whenValidData() {
        val email = "new@example.com"
        val login = "newuser"
        val passwordHash = "newhash"

        val result = userRepository.saveNew(email, login, passwordHash)

        assertThat(result.id).isNotNull
        assertThat(result.email).isEqualTo(email)
        assertThat(result.login).isEqualTo(login)
        assertThat(result.passwordHash).isEqualTo(passwordHash)
    }

    @Test
    fun update_shouldModifyUser() {
        val userId = createTestUser(email = "old@example.com", login = "oldlogin")
        val user = userRepository.findById(userId)!!

        val updated = userRepository.update(user.copy(email = "new@example.com", login = "newlogin"))

        assertThat(updated.email).isEqualTo("new@example.com")
        assertThat(updated.login).isEqualTo("newlogin")
        val fromDb = userRepository.findById(userId)
        assertThat(fromDb!!.email).isEqualTo("new@example.com")
        assertThat(fromDb.login).isEqualTo("newlogin")
    }

    @Test
    fun updatePassword_shouldChangePassword() {
        val userId = createTestUser()
        userRepository.updatePassword(userId, "newHash")

        val user = userRepository.findById(userId)
        assertThat(user!!.passwordHash).isEqualTo("newHash")
    }

    @Test
    fun delete_shouldRemoveUser() {
        val userId = createTestUser()

        userRepository.delete(userId)

        assertThat(userRepository.findById(userId)).isNull()
        assertThat(userRepository.findByLogin("testuser")).isNull()
    }

    private fun createTestUser(
        email: String = "test@example.com",
        login: String = "testuser",
    ): UUID = userRepository.saveNew(email, login, "hash").id
}
