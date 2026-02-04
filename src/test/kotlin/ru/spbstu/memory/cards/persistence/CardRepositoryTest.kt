package ru.spbstu.memory.cards.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.spbstu.memory.cards.persistence.config.BasePostgresTest
import java.util.UUID

class CardRepositoryTest : BasePostgresTest() {
    @Test
    fun findById_shouldReturnCard_whenCardExists() {
        val deckId = createTestDeck()
        val card = cardRepository.saveNew(deckId, "Q?", "A!")

        val result = cardRepository.findById(card.id)

        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(card.id)
        assertThat(result.question).isEqualTo("Q?")
        assertThat(result.answer).isEqualTo("A!")
        assertThat(result.deckId).isEqualTo(deckId)
    }

    @Test
    fun findById_shouldReturnNull_whenCardNotExists() {
        val result = cardRepository.findById(UUID.randomUUID())

        assertThat(result).isNull()
    }

    @Test
    fun findAllByDeckIdPaginated_shouldReturnPaginatedCards() {
        val deckId = createTestDeck()
        cardRepository.saveNew(deckId, "Q1", "A1")
        cardRepository.saveNew(deckId, "Q2", "A2")
        cardRepository.saveNew(deckId, "Q3", "A3")

        val result = cardRepository.findAllByDeckIdPaginated(deckId, 0, 2)

        assertThat(result.items).hasSize(2)
        assertThat(result.total).isEqualTo(3L)
    }

    @Test
    fun findAllByDeckIdPaginated_shouldRespectPageAndSize() {
        val deckId = createTestDeck()
        repeat(5) { i -> cardRepository.saveNew(deckId, "Q$i", "A$i") }

        val page0 = cardRepository.findAllByDeckIdPaginated(deckId, 0, 2)
        val page1 = cardRepository.findAllByDeckIdPaginated(deckId, 1, 2)

        assertThat(page0.items).hasSize(2)
        assertThat(page0.total).isEqualTo(5L)
        assertThat(page1.items).hasSize(2)
        assertThat(page1.total).isEqualTo(5L)
        assertThat(page0.items.map { it.id }).doesNotContainAnyElementsOf(page1.items.map { it.id })
    }

    @Test
    fun findAllByDeckId_shouldReturnAllCardsInOrder() {
        val deckId = createTestDeck()
        cardRepository.saveNew(deckId, "Q1", "A1")
        cardRepository.saveNew(deckId, "Q2", "A2")

        val result = cardRepository.findAllByDeckId(deckId)

        assertThat(result).hasSize(2)
        assertThat(result.map { it.question }).containsExactly("Q1", "Q2")
    }

    @Test
    fun getLearnedCount_shouldReturnCount_whenLearnedCardsExist() {
        val deckId = createTestDeck()
        val c1 = cardRepository.saveNew(deckId, "Q1", "A1")
        val c2 = cardRepository.saveNew(deckId, "Q2", "A2")
        cardRepository.update(c1.copy(isLearned = true))
        cardRepository.update(c2.copy(isLearned = true))

        val result = cardRepository.getLearnedCount(deckId)

        assertThat(result).isEqualTo(2L)
    }

    @Test
    fun saveNew_shouldInsertCard() {
        val deckId = createTestDeck()

        val result = cardRepository.saveNew(deckId, "Question", "Answer")

        assertThat(result.id).isNotNull
        assertThat(result.deckId).isEqualTo(deckId)
        assertThat(result.question).isEqualTo("Question")
        assertThat(result.answer).isEqualTo("Answer")
        assertThat(result.isLearned).isFalse()
        val fromDb = cardRepository.findById(result.id)
        assertThat(fromDb).isNotNull
        assertThat(fromDb!!.question).isEqualTo("Question")
    }

    @Test
    fun copyAllToDeck_shouldCopyCardsToTargetDeck() {
        val deckId1 = createTestDeck()
        val deckId2 = createTestDeck(email = "user2@example.com")
        cardRepository.saveNew(deckId1, "Q1", "A1")
        cardRepository.saveNew(deckId1, "Q2", "A2")

        val inserted = cardRepository.copyAllToDeck(deckId1, deckId2)

        assertThat(inserted).isEqualTo(2)
        val targetCards = cardRepository.findAllByDeckId(deckId2)
        assertThat(targetCards).hasSize(2)
        assertThat(targetCards.map { it.question }).containsExactlyInAnyOrder("Q1", "Q2")
    }

    @Test
    fun copyAllToDeck_shouldCopyContentButNotLearnedState() {
        val deckId1 = createTestDeck()
        val deckId2 = createTestDeck(email = "user2@example.com")
        val c1 = cardRepository.saveNew(deckId1, "Q1", "A1")
        cardRepository.update(c1.copy(isLearned = true))

        cardRepository.copyAllToDeck(deckId1, deckId2)

        val copied = cardRepository.findAllByDeckId(deckId2)
        assertThat(copied).hasSize(1)
        assertThat(copied.single().question).isEqualTo("Q1")
        assertThat(copied.single().isLearned).isFalse()
    }

    @Test
    fun update_shouldModifyCard() {
        val deckId = createTestDeck()
        val card = cardRepository.saveNew(deckId, "Q", "A")

        val updated =
            cardRepository.update(
                card.copy(
                    question = "New Q",
                    answer = "New A",
                    isLearned = true,
                ),
            )

        assertThat(updated.question).isEqualTo("New Q")
        assertThat(updated.answer).isEqualTo("New A")
        assertThat(updated.isLearned).isTrue()
        val fromDb = cardRepository.findById(card.id)
        assertThat(fromDb!!.question).isEqualTo("New Q")
    }

    @Test
    fun delete_shouldRemoveCard() {
        val deckId = createTestDeck()
        val card = cardRepository.saveNew(deckId, "Q", "A")

        cardRepository.delete(card.id)

        assertThat(cardRepository.findById(card.id)).isNull()
    }

    @Test
    fun findAllByDeckId_shouldNotReturnCardsFromOtherDeck() {
        val deckId1 = createTestDeck()
        val deckId2 = createTestDeck(email = "user2@example.com")
        cardRepository.saveNew(deckId1, "Q1", "A1")
        cardRepository.saveNew(deckId1, "Q2", "A2")
        cardRepository.saveNew(deckId2, "Q3", "A3")

        val result = cardRepository.findAllByDeckId(deckId1)

        assertThat(result).hasSize(2)
        assertThat(result.map { it.question }).containsExactlyInAnyOrder("Q1", "Q2")
        assertThat(result.map { it.deckId }).containsOnly(deckId1)
    }

    private fun createTestDeck(email: String = "user@example.com"): UUID {
        val userId = userRepository.saveNew(email, "user${UUID.randomUUID()}", "hash").id
        return deckRepository.saveNew(userId, "Deck", null).id
    }
}
