package ru.spbstu.memory.cards.persistence

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.spbstu.memory.cards.persistence.config.BasePostgresTest
import ru.spbstu.memory.cards.persistence.mapper.toDeck
import ru.spbstu.memory.cards.persistence.table.DeckTable
import ru.spbstu.memory.cards.persistence.table.UserTable
import java.time.OffsetDateTime
import java.util.UUID

class DeckRepositoryTest : BasePostgresTest() {
    @Autowired
    private lateinit var deckRepository: DeckRepository

    private fun createTestUser(): UUID {
        return transaction {
            val userId = UUID.randomUUID()
            UserTable.insert {
                it[UserTable.id] = userId
                it[UserTable.email] = "test@example.com"
                it[UserTable.login] = "testuser"
                it[UserTable.password] = "hash"
                it[UserTable.createdAt] = OffsetDateTime.now()
                it[UserTable.updatedAt] = OffsetDateTime.now()
            }
            userId
        }
    }

    @Test
    fun findById_shouldReturnDeck_whenDeckExists() {
        val userId = createTestUser()
        val deckId = UUID.randomUUID()
        transaction {
            DeckTable.insert {
                it[DeckTable.id] = deckId
                it[DeckTable.userId] = userId
                it[DeckTable.name] = "Test Deck"
                it[DeckTable.description] = "Test Description"
                it[DeckTable.learnedPercent] = 50
                it[DeckTable.cardsCount] = 10
                it[DeckTable.lastStudied] = null
                it[DeckTable.createdAt] = OffsetDateTime.now()
                it[DeckTable.updatedAt] = OffsetDateTime.now()
            }
        }

        val result = deckRepository.findById(deckId)

        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(deckId)
        assertThat(result.name).isEqualTo("Test Deck")
        assertThat(result.description).isEqualTo("Test Description")
    }

    @Test
    fun findById_shouldReturnNull_whenDeckNotExists() {
        val result = deckRepository.findById(UUID.randomUUID())

        assertThat(result).isNull()
    }

    @Test
    fun findAllByUserId_shouldReturnDecks_whenDecksExist() {
        val userId = createTestUser()
        val deckId1 = UUID.randomUUID()
        val deckId2 = UUID.randomUUID()
        transaction {
            DeckTable.insert {
                it[DeckTable.id] = deckId1
                it[DeckTable.userId] = userId
                it[DeckTable.name] = "Deck 1"
                it[DeckTable.description] = null
                it[DeckTable.learnedPercent] = 0
                it[DeckTable.cardsCount] = 0
                it[DeckTable.lastStudied] = null
                it[DeckTable.createdAt] = OffsetDateTime.now()
                it[DeckTable.updatedAt] = OffsetDateTime.now()
            }
            DeckTable.insert {
                it[DeckTable.id] = deckId2
                it[DeckTable.userId] = userId
                it[DeckTable.name] = "Deck 2"
                it[DeckTable.description] = null
                it[DeckTable.learnedPercent] = 0
                it[DeckTable.cardsCount] = 0
                it[DeckTable.lastStudied] = null
                it[DeckTable.createdAt] = OffsetDateTime.now()
                it[DeckTable.updatedAt] = OffsetDateTime.now()
            }
        }
//
//        val result = deckRepository.findAllByUserId(userId)
//
//        assertThat(result).hasSize(2)
//        assertThat(result.map { it.id }).containsExactlyInAnyOrder(deckId1, deckId2)
    }

//    @Test
//    fun findAllByUserId_shouldReturnEmptyList_whenNoDecksExist() {
//        val userId = createTestUser()
//
//        val result = deckRepository.findAllByUserId(userId)
//
//        assertThat(result).isEmpty()
//    }

    @Test
    fun saveNew_shouldInsertDeck_whenValidData() {
        val userId = createTestUser()
        val name = "New Deck"
        val description = "New Description"

        val result = deckRepository.saveNew(userId, name, description)

        val savedDeck =
            transaction {
                DeckTable
                    .selectAll()
                    .where { DeckTable.id eq result.id }
                    .firstOrNull()
                    ?.toDeck()
            }

        assertThat(savedDeck).isNotNull
        assertThat(savedDeck!!.userId).isEqualTo(userId)
        assertThat(savedDeck.name).isEqualTo(name)
        assertThat(savedDeck.description).isEqualTo(description)
        assertThat(savedDeck.learnedPercent).isEqualTo(0)
        assertThat(savedDeck.cardsCount).isEqualTo(0)
        assertThat(savedDeck.id).isEqualTo(result.id)
    }

    @Test
    fun saveNew_shouldInsertDeck_whenDescriptionIsNull() {
        val userId = createTestUser()
        val name = "Deck Without Description"

        val result = deckRepository.saveNew(userId, name, null)

        val savedDeck =
            transaction {
                DeckTable
                    .selectAll()
                    .where { DeckTable.id eq result.id }
                    .firstOrNull()
                    ?.toDeck()
            }

        assertThat(savedDeck).isNotNull
        assertThat(savedDeck!!.description).isNull()
    }
}
