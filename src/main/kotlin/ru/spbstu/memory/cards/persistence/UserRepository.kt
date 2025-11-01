package ru.spbstu.memory.cards.persistence

import org.springframework.stereotype.Repository
import ru.spbstu.memory.cards.persistence.model.User
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// todo переделать на работу с бд
@Repository
class UserRepository {
    private val loginIndex = ConcurrentHashMap<String, UUID>()

    private val emailIndex = ConcurrentHashMap<String, UUID>()

    private val storage = ConcurrentHashMap<UUID, User>()

    fun findByLogin(login: String): User? {
        val id = loginIndex[login] ?: return null
        return storage[id]
    }

    fun existsByLogin(login: String): Boolean = loginIndex.containsKey(login)

    fun existsByEmail(email: String): Boolean = emailIndex.containsKey(email)

    fun saveNew(
        email: String,
        login: String,
        passwordHash: String,
    ): User {
        val now = OffsetDateTime.now()
        val id = UUID.randomUUID()
        val user =
            User(
                id = id,
                email = email,
                login = login,
                passwordHash = passwordHash,
                createdAt = now,
                updatedAt = now,
            )

        synchronized(this) {
            if (emailIndex.containsKey(email)) {
                throw IllegalStateException("email_already_exists")
            }
            if (loginIndex.containsKey(login)) {
                throw IllegalStateException("login_already_exists")
            }

            storage[id] = user
            emailIndex[email] = id
            loginIndex[login] = id
        }

        return user
    }
}
