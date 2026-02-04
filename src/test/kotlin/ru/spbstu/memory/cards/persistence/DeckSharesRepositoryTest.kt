package ru.spbstu.memory.cards.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.spbstu.memory.cards.persistence.config.BasePostgresTest
import java.util.UUID

class DeckSharesRepositoryTest : BasePostgresTest() {
    @Test
    fun saveToken_shouldPersistTokenAndDeckId() {
        val deckId = createTestDeck()
        val token = UUID.randomUUID()

        shareRepository.saveToken(token, deckId)

        val result = shareRepository.findByToken(token)
        assertThat(result).isEqualTo(deckId)
    }

    @Test
    fun findByToken_shouldReturnDeckId_whenTokenExists() {
        val deckId = createTestDeck()
        val token = UUID.randomUUID()
        shareRepository.saveToken(token, deckId)

        val result = shareRepository.findByToken(token)

        assertThat(result).isEqualTo(deckId)
    }

    @Test
    fun findByToken_shouldReturnNull_whenTokenNotExists() {
        val result = shareRepository.findByToken(UUID.randomUUID())

        assertThat(result).isNull()
    }

    @Test
    fun findByToken_shouldReturnCorrectDeckId_whenMultipleTokensExist() {
        val deckId1 = createTestDeck()
        val deckId2 = createTestDeck(email = "user2@example.com")
        val token1 = UUID.randomUUID()
        val token2 = UUID.randomUUID()
        shareRepository.saveToken(token1, deckId1)
        shareRepository.saveToken(token2, deckId2)

        assertThat(shareRepository.findByToken(token1)).isEqualTo(deckId1)
        assertThat(shareRepository.findByToken(token2)).isEqualTo(deckId2)
    }

    @Test
    fun deleteToken_shouldRemoveToken() {
        val deckId = createTestDeck()
        val token = UUID.randomUUID()
        shareRepository.saveToken(token, deckId)

        shareRepository.deleteToken(token)

        assertThat(shareRepository.findByToken(token)).isNull()
    }

    @Test
    fun deleteToken_shouldNotAffectOtherTokens() {
        val deckId1 = createTestDeck()
        val deckId2 = createTestDeck(email = "user2@example.com")
        val token1 = UUID.randomUUID()
        val token2 = UUID.randomUUID()
        shareRepository.saveToken(token1, deckId1)
        shareRepository.saveToken(token2, deckId2)

        shareRepository.deleteToken(token1)

        assertThat(shareRepository.findByToken(token1)).isNull()
        assertThat(shareRepository.findByToken(token2)).isEqualTo(deckId2)
    }

    @Test
    fun saveToken_shouldAllowSameDeckMultipleTokens() {
        val deckId = createTestDeck()
        val token1 = UUID.randomUUID()
        val token2 = UUID.randomUUID()
        shareRepository.saveToken(token1, deckId)
        shareRepository.saveToken(token2, deckId)

        assertThat(shareRepository.findByToken(token1)).isEqualTo(deckId)
        assertThat(shareRepository.findByToken(token2)).isEqualTo(deckId)
    }

    private fun createTestDeck(email: String = "user@example.com"): UUID {
        val userId = userRepository.saveNew(email, "user${UUID.randomUUID()}", "hash").id
        return deckRepository.saveNew(userId, "Deck", null).id
    }
}
