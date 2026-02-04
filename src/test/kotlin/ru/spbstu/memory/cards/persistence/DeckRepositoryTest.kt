package ru.spbstu.memory.cards.persistence

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import ru.spbstu.memory.cards.persistence.config.BasePostgresTest
import ru.spbstu.memory.cards.persistence.table.DeckTable
import ru.spbstu.memory.cards.persistence.table.UserTable
import java.time.OffsetDateTime
import java.util.UUID

class DeckRepositoryTest : BasePostgresTest() {
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
    fun findAllByUserId_shouldReturnPaginatedDecks_whenDecksExist() {
        val userId = createTestUser()
        val deckId1 = UUID.randomUUID()
        val deckId2 = UUID.randomUUID()
        val now = OffsetDateTime.now()
        transaction {
            DeckTable.insert {
                it[DeckTable.id] = deckId1
                it[DeckTable.userId] = userId
                it[DeckTable.name] = "Deck 1"
                it[DeckTable.description] = null
                it[DeckTable.learnedPercent] = 0
                it[DeckTable.cardsCount] = 0
                it[DeckTable.lastStudied] = null
                it[DeckTable.createdAt] = now
                it[DeckTable.updatedAt] = now
            }
            DeckTable.insert {
                it[DeckTable.id] = deckId2
                it[DeckTable.userId] = userId
                it[DeckTable.name] = "Deck 2"
                it[DeckTable.description] = null
                it[DeckTable.learnedPercent] = 0
                it[DeckTable.cardsCount] = 0
                it[DeckTable.lastStudied] = null
                it[DeckTable.createdAt] = now
                it[DeckTable.updatedAt] = now
            }
        }

        val result = deckRepository.findAllByUserId(userId, 0, 20)

        assertThat(result.items).hasSize(2)
        assertThat(result.total).isEqualTo(2L)
        assertThat(result.items.map { it.id }).containsExactlyInAnyOrder(deckId1, deckId2)
    }

    @Test
    fun findAllByUserId_shouldRespectPagination() {
        val userId = createTestUser()
        val now = OffsetDateTime.now()
        transaction {
            repeat(5) { i ->
                DeckTable.insert {
                    it[DeckTable.id] = UUID.randomUUID()
                    it[DeckTable.userId] = userId
                    it[DeckTable.name] = "Deck $i"
                    it[DeckTable.description] = null
                    it[DeckTable.learnedPercent] = 0
                    it[DeckTable.cardsCount] = 0
                    it[DeckTable.lastStudied] = null
                    it[DeckTable.createdAt] = now
                    it[DeckTable.updatedAt] = now
                }
            }
        }

        val page0 = deckRepository.findAllByUserId(userId, 0, 2)
        val page1 = deckRepository.findAllByUserId(userId, 1, 2)

        assertThat(page0.items).hasSize(2)
        assertThat(page0.total).isEqualTo(5L)
        assertThat(page1.items).hasSize(2)
        assertThat(page1.total).isEqualTo(5L)
        assertThat(page0.items.map { it.id }).doesNotContainAnyElementsOf(page1.items.map { it.id })
    }

    @Test
    fun existsByUserIdAndName_shouldReturnTrue_whenDeckExists() {
        val userId = createTestUser()
        val deckId = UUID.randomUUID()
        val now = OffsetDateTime.now()
        transaction {
            DeckTable.insert {
                it[DeckTable.id] = deckId
                it[DeckTable.userId] = userId
                it[DeckTable.name] = "My Deck"
                it[DeckTable.description] = null
                it[DeckTable.learnedPercent] = 0
                it[DeckTable.cardsCount] = 0
                it[DeckTable.lastStudied] = null
                it[DeckTable.createdAt] = now
                it[DeckTable.updatedAt] = now
            }
        }

        val result = deckRepository.existsByUserIdAndName(userId, "My Deck")

        assertThat(result).isTrue()
    }

    @Test
    fun existsByUserIdAndName_shouldReturnFalse_whenDeckNotExists() {
        val userId = createTestUser()

        val result = deckRepository.existsByUserIdAndName(userId, "Nonexistent")

        assertThat(result).isFalse()
    }

    @Test
    fun existsByUserIdAndNameAndId_shouldReturnFalse_whenAnotherDeckHasSameName() {
        val userId = createTestUser()
        val deckId1 = UUID.randomUUID()
        val deckId2 = UUID.randomUUID()
        val now = OffsetDateTime.now()
        transaction {
            DeckTable.insert {
                it[DeckTable.id] = deckId1
                it[DeckTable.userId] = userId
                it[DeckTable.name] = "Same Name"
                it[DeckTable.description] = null
                it[DeckTable.learnedPercent] = 0
                it[DeckTable.cardsCount] = 0
                it[DeckTable.lastStudied] = null
                it[DeckTable.createdAt] = now
                it[DeckTable.updatedAt] = now
            }
            DeckTable.insert {
                it[DeckTable.id] = deckId2
                it[DeckTable.userId] = userId
                it[DeckTable.name] = "Same Name"
                it[DeckTable.description] = null
                it[DeckTable.learnedPercent] = 0
                it[DeckTable.cardsCount] = 0
                it[DeckTable.lastStudied] = null
                it[DeckTable.createdAt] = now
                it[DeckTable.updatedAt] = now
            }
        }

        val result = deckRepository.existsByUserIdAndNameAndId(userId, "Same Name", deckId1)

        assertThat(result).isFalse()
    }

    @Test
    fun update_shouldModifyDeck() {
        val userId = createTestUser()
        val deck = deckRepository.saveNew(userId, "Original", "Desc")
        val updated =
            deck.copy(
                name = "Updated Name",
                description = "Updated Desc",
                cardsCount = 5,
                learnedPercent = 50,
            )

        val result = deckRepository.update(updated)

        assertThat(result.name).isEqualTo("Updated Name")
        assertThat(result.description).isEqualTo("Updated Desc")
        assertThat(result.cardsCount).isEqualTo(5)
        assertThat(result.learnedPercent).isEqualTo(50)
        val fromDb = deckRepository.findById(deck.id)
        assertThat(fromDb!!.name).isEqualTo("Updated Name")
    }

    @Test
    fun delete_shouldRemoveDeck() {
        val userId = createTestUser()
        val deck = deckRepository.saveNew(userId, "To Delete", null)

        deckRepository.delete(deck.id)

        assertThat(deckRepository.findById(deck.id)).isNull()
        assertThat(deckRepository.existsById(deck.id)).isFalse()
    }

    @Test
    fun saveNew_shouldInsertDeck_whenValidData() {
        val userId = createTestUser()
        val name = "New Deck"
        val description = "New Description"

        val result = deckRepository.saveNew(userId, name, description)

        assertThat(result.id).isNotNull
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.name).isEqualTo(name)
        assertThat(result.description).isEqualTo(description)
        assertThat(result.learnedPercent).isEqualTo(0)
        assertThat(result.cardsCount).isEqualTo(0)
    }

    @Test
    fun findAllByUserId_shouldReturnOnlyThisUserDecks() {
        val userId1 = createTestUser()
        val userId2 =
            transaction {
                val id = UUID.randomUUID()
                UserTable.insert {
                    it[UserTable.id] = id
                    it[UserTable.email] = "other@example.com"
                    it[UserTable.login] = "otheruser"
                    it[UserTable.password] = "hash"
                    it[UserTable.createdAt] = OffsetDateTime.now()
                    it[UserTable.updatedAt] = OffsetDateTime.now()
                }
                id
            }
        val now = OffsetDateTime.now()
        transaction {
            DeckTable.insert {
                it[DeckTable.id] = UUID.randomUUID()
                it[DeckTable.userId] = userId1
                it[DeckTable.name] = "User1 Deck"
                it[DeckTable.description] = null
                it[DeckTable.learnedPercent] = 0
                it[DeckTable.cardsCount] = 0
                it[DeckTable.lastStudied] = null
                it[DeckTable.createdAt] = now
                it[DeckTable.updatedAt] = now
            }
            DeckTable.insert {
                it[DeckTable.id] = UUID.randomUUID()
                it[DeckTable.userId] = userId2
                it[DeckTable.name] = "User2 Deck"
                it[DeckTable.description] = null
                it[DeckTable.learnedPercent] = 0
                it[DeckTable.cardsCount] = 0
                it[DeckTable.lastStudied] = null
                it[DeckTable.createdAt] = now
                it[DeckTable.updatedAt] = now
            }
        }

        val result = deckRepository.findAllByUserId(userId1, 0, 20)

        assertThat(result.items).hasSize(1)
        assertThat(result.items.single().name).isEqualTo("User1 Deck")
        assertThat(result.total).isEqualTo(1L)
    }
}
