package ru.spbstu.memory.cards.persistence

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.spbstu.memory.cards.persistence.config.BasePostgresTest
import ru.spbstu.memory.cards.persistence.mapper.toCard
import ru.spbstu.memory.cards.persistence.table.CardTable
import ru.spbstu.memory.cards.persistence.table.DeckTable
import ru.spbstu.memory.cards.persistence.table.UserTable
import java.time.OffsetDateTime
import java.util.UUID

class CardRepositoryTest : BasePostgresTest() {
    @Autowired
    private lateinit var cardRepository: CardRepository

    private fun createTestDeck(): UUID {
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

            val deckId = UUID.randomUUID()
            DeckTable.insert {
                it[DeckTable.id] = deckId
                it[DeckTable.userId] = userId
                it[DeckTable.name] = "Test Deck"
                it[DeckTable.description] = null
                it[DeckTable.learnedPercent] = 0
                it[DeckTable.cardsCount] = 0
                it[DeckTable.lastStudied] = null
                it[DeckTable.createdAt] = OffsetDateTime.now()
                it[DeckTable.updatedAt] = OffsetDateTime.now()
            }
            deckId
        }
    }

    @Test
    fun findById_shouldReturnCard_whenCardExists() {
        val deckId = createTestDeck()
        val cardId = UUID.randomUUID()
        transaction {
            CardTable.insert {
                it[CardTable.id] = cardId
                it[CardTable.deckId] = deckId
                it[CardTable.question] = "Test Question"
                it[CardTable.answer] = "Test Answer"
                it[CardTable.createdAt] = OffsetDateTime.now()
                it[CardTable.updatedAt] = OffsetDateTime.now()
            }
        }

        val result = cardRepository.findById(cardId)

        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(cardId)
        assertThat(result.question).isEqualTo("Test Question")
        assertThat(result.answer).isEqualTo("Test Answer")
    }

    @Test
    fun findByIdShould_returnNull_whenCardNotExists() {
        val result = cardRepository.findById(UUID.randomUUID())

        assertThat(result).isNull()
    }

    @Test
    fun findAllByDeckId_shouldReturnCards_whenCardsExist() {
        val deckId = createTestDeck()
        val cardId1 = UUID.randomUUID()
        val cardId2 = UUID.randomUUID()
        transaction {
            CardTable.insert {
                it[CardTable.id] = cardId1
                it[CardTable.deckId] = deckId
                it[CardTable.question] = "Question 1"
                it[CardTable.answer] = "Answer 1"
                it[CardTable.createdAt] = OffsetDateTime.now()
                it[CardTable.updatedAt] = OffsetDateTime.now()
            }
            CardTable.insert {
                it[CardTable.id] = cardId2
                it[CardTable.deckId] = deckId
                it[CardTable.question] = "Question 2"
                it[CardTable.answer] = "Answer 2"
                it[CardTable.createdAt] = OffsetDateTime.now()
                it[CardTable.updatedAt] = OffsetDateTime.now()
            }
        }

        val result = cardRepository.findAllByDeckId(deckId)

        assertThat(result).hasSize(2)
        assertThat(result.map { it.id }).containsExactlyInAnyOrder(cardId1, cardId2)
    }

    @Test
    fun findAllByDeckId_shouldReturnEmptyList_whenNoCardsExist() {
        val deckId = createTestDeck()

        val result = cardRepository.findAllByDeckId(deckId)

        assertThat(result).isEmpty()
    }

    @Test
    fun saveNew_shouldInsertCard_whenValidData() {
        val deckId = createTestDeck()
        val question = "New Question"
        val answer = "New Answer"

        val result = cardRepository.saveNew(deckId, question, answer)

        val savedCard =
            transaction {
                CardTable
                    .selectAll()
                    .where { CardTable.id eq result.id }
                    .firstOrNull()
                    ?.toCard()
            }

        assertThat(savedCard).isNotNull
        assertThat(savedCard!!.deckId).isEqualTo(deckId)
        assertThat(savedCard.question).isEqualTo(question)
        assertThat(savedCard.answer).isEqualTo(answer)
        assertThat(savedCard.id).isEqualTo(result.id)
    }
}
