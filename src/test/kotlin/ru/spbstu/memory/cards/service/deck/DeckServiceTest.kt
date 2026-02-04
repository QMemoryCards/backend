package ru.spbstu.memory.cards.service.deck

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.spbstu.memory.cards.dto.request.CreateDeckRequest
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.domain.ConflictException
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.LimitExceededException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.persistence.DeckRepository
import ru.spbstu.memory.cards.persistence.model.Deck
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class DeckServiceTest(
    @Mock private val deckRepository: DeckRepository,
) {
    private val deckService = DeckService(deckRepository)

    private val userId = UUID.randomUUID()
    private val deckId = UUID.randomUUID()
    private val deck =
        Deck(
            id = deckId,
            userId = userId,
            name = "Test Deck",
            description = "Test Description",
            learnedPercent = 0,
            cardsCount = 0,
            lastStudied = null,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
        )
    private val createRequest =
        CreateDeckRequest(
            name = "New Deck",
            description = "New Description",
        )

    @Test
    fun createDeck_shouldCreateDeck_whenLimitNotExceededAndNameUnique() {
        whenever(deckRepository.countByUserId(userId)).thenReturn(5L)
        whenever(deckRepository.existsByUserIdAndName(userId, createRequest.name)).thenReturn(false)
        whenever(deckRepository.saveNew(userId, createRequest.name, createRequest.description))
            .thenReturn(deck)

        val result = deckService.createDeck(userId, createRequest)

        assertThat(result.name).isEqualTo(deck.name)
        verify(deckRepository).countByUserId(userId)
        verify(deckRepository).existsByUserIdAndName(userId, createRequest.name)
        verify(deckRepository).saveNew(userId, createRequest.name, createRequest.description)
    }

    @Test
    fun createDeck_shouldThrowLimitExceededException_whenDeckLimitReached() {
        whenever(deckRepository.countByUserId(userId)).thenReturn(DeckService.DECK_LIMIT.toLong())

        assertThatThrownBy { deckService.createDeck(userId, createRequest) }
            .isInstanceOf(LimitExceededException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.DECK_LIMIT)

        verify(deckRepository, never()).saveNew(any(), any(), any())
    }

    @Test
    fun createDeck_shouldThrowConflictException_whenDeckNameExists() {
        whenever(deckRepository.countByUserId(userId)).thenReturn(5L)
        whenever(deckRepository.existsByUserIdAndName(userId, createRequest.name)).thenReturn(true)

        assertThatThrownBy { deckService.createDeck(userId, createRequest) }
            .isInstanceOf(ConflictException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.DECK_CONFLICT)

        verify(deckRepository, never()).saveNew(any(), any(), any())
    }

    @Test
    fun getDeck_shouldReturnDeck_whenUserOwnsIt() {
        whenever(deckRepository.findById(deckId)).thenReturn(deck)

        val result = deckService.getDeck(deckId, userId)

        assertThat(result.id).isEqualTo(deckId)
        verify(deckRepository).findById(deckId)
    }

    @Test
    fun getDeck_shouldMapLastStudied_whenPresent() {
        val deckWithLastStudied =
            deck.copy(
                lastStudied = OffsetDateTime.parse("2026-01-01T10:15:30+03:00"),
                createdAt = OffsetDateTime.parse("2026-01-01T00:00:00+03:00"),
                updatedAt = OffsetDateTime.parse("2026-01-02T00:00:00+03:00"),
            )

        whenever(deckRepository.findById(deckId)).thenReturn(deckWithLastStudied)

        val result = deckService.getDeck(deckId, userId)

        assertThat(result.lastStudied).isEqualTo("2026-01-01T10:15:30+03:00")
        assertThat(result.createdAt).isEqualTo("2026-01-01T00:00:00+03:00")
        assertThat(result.updatedAt).isEqualTo("2026-01-02T00:00:00+03:00")

        verify(deckRepository).findById(deckId)
    }

    @Test
    fun getDeck_shouldThrowNotFoundException_whenDeckNotExists() {
        whenever(deckRepository.findById(deckId)).thenReturn(null)

        assertThatThrownBy { deckService.getDeck(deckId, userId) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.DECK_NOT_FOUND)
    }

    @Test
    fun getDeck_shouldThrowForbiddenException_whenUserNotOwnsDeck() {
        val otherUserId = UUID.randomUUID()
        whenever(deckRepository.findById(deckId)).thenReturn(deck)

        assertThatThrownBy { deckService.getDeck(deckId, otherUserId) }
            .isInstanceOf(ForbiddenException::class.java)
    }

    @Test
    fun updateDeck_shouldUpdateDeck_whenUserOwnsItAndNameUnique() {
        val updateRequest =
            CreateDeckRequest(
                name = "Updated Deck",
                description = "Updated Description",
            )

        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(deckRepository.existsByUserIdAndNameAndId(userId, updateRequest.name, deckId)).thenReturn(true)
        whenever(deckRepository.update(any())).thenAnswer { invocation ->
            val updatedDeck = invocation.getArgument<Deck>(0)
            updatedDeck.copy(updatedAt = OffsetDateTime.now())
        }

        val result = deckService.updateDeck(deckId, userId, updateRequest)

        assertThat(result.name).isEqualTo(updateRequest.name)
        assertThat(result.description).isEqualTo(updateRequest.description)
        verify(deckRepository).update(any())
    }

    @Test
    fun updateDeck_shouldThrowConflictException_whenNameTakenByAnotherDeck() {
        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(deckRepository.existsByUserIdAndNameAndId(userId, createRequest.name, deckId)).thenReturn(false)

        assertThatThrownBy { deckService.updateDeck(deckId, userId, createRequest) }
            .isInstanceOf(ConflictException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.DECK_CONFLICT)

        verify(deckRepository, never()).update(any())
    }

    @Test
    fun deleteDeck_shouldDeleteDeck_whenUserOwnsIt() {
        whenever(deckRepository.findById(deckId)).thenReturn(deck)

        deckService.deleteDeck(deckId, userId)

        verify(deckRepository).findById(deckId)
        verify(deckRepository).delete(deckId)
    }

    @Test
    fun deleteDeck_shouldThrowNotFoundException_whenDeckNotExists() {
        whenever(deckRepository.findById(deckId)).thenReturn(null)

        assertThatThrownBy { deckService.deleteDeck(deckId, userId) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.DECK_NOT_FOUND)

        verify(deckRepository, never()).delete(any())
    }
}
