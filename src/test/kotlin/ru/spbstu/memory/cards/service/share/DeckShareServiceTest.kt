package ru.spbstu.memory.cards.service.share

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.spbstu.memory.cards.dto.request.CreateDeckRequest
import ru.spbstu.memory.cards.dto.request.ImportSharedDeckRequest
import ru.spbstu.memory.cards.dto.response.DeckResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.persistence.CardRepository
import ru.spbstu.memory.cards.persistence.DeckRepository
import ru.spbstu.memory.cards.persistence.DeckSharesRepository
import ru.spbstu.memory.cards.persistence.model.Deck
import ru.spbstu.memory.cards.service.deck.DeckService
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class DeckShareServiceTest(
    @Mock private val deckService: DeckService,
    @Mock private val shareRepository: DeckSharesRepository,
    @Mock private val deckRepository: DeckRepository,
    @Mock private val cardRepository: CardRepository,
) {
    private val deckShareService = DeckShareService(deckService, shareRepository, deckRepository, cardRepository)

    private val userId = UUID.randomUUID()
    private val deckId = UUID.randomUUID()
    private val token = UUID.randomUUID()
    private val deck =
        Deck(
            id = deckId,
            userId = userId,
            name = "Test Deck",
            description = "Test Description",
            learnedPercent = 0,
            cardsCount = 5,
            lastStudied = null,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
        )

    @Test
    fun generateShareToken_shouldCreateShareToken_whenUserOwnsDeck() {
        whenever(deckRepository.findById(deckId)).thenReturn(deck)

        val result = deckShareService.generateShareToken(deckId, userId)

        assertThat(result.token).isNotNull
        assertThat(result.url).contains(result.token)
        verify(deckRepository).findById(deckId)
        verify(shareRepository).saveToken(any(), eq(deckId))
    }

    @Test
    fun generateShareToken_shouldThrowNotFoundException_whenDeckNotExists() {
        whenever(deckRepository.findById(deckId)).thenReturn(null)

        assertThatThrownBy { deckShareService.generateShareToken(deckId, userId) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.DECK_NOT_FOUND)

        verify(shareRepository, never()).saveToken(any(), any())
    }

    @Test
    fun generateShareToken_shouldThrowForbiddenException_whenUserNotOwnsDeck() {
        val otherUserId = UUID.randomUUID()
        whenever(deckRepository.findById(deckId)).thenReturn(deck)

        assertThatThrownBy { deckShareService.generateShareToken(deckId, otherUserId) }
            .isInstanceOf(ForbiddenException::class.java)

        verify(shareRepository, never()).saveToken(any(), any())
    }

    @Test
    fun getSharedDeck_shouldReturnDeckShareInfo_whenTokenExists() {
        whenever(shareRepository.findByToken(token)).thenReturn(deckId)
        whenever(deckRepository.findById(deckId)).thenReturn(deck)

        val result = deckShareService.getSharedDeck(token)

        assertThat(result.name).isEqualTo(deck.name)
        assertThat(result.description).isEqualTo(deck.description)
        assertThat(result.cardCount).isEqualTo(deck.cardsCount)
        verify(shareRepository).findByToken(token)
        verify(deckRepository).findById(deckId)
    }

    @Test
    fun getSharedDeck_shouldThrowNotFoundException_whenTokenNotExists() {
        whenever(shareRepository.findByToken(token)).thenReturn(null)

        assertThatThrownBy { deckShareService.getSharedDeck(token) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.TOKEN_NOT_FOUND)

        verify(deckRepository, never()).findById(any())
    }

    @Test
    fun importSharedDeck_shouldImportDeckWithCustomName_whenRequestProvided() {
        val newUserId = UUID.randomUUID()
        val newDeckId = UUID.randomUUID()

        val importRequest =
            ImportSharedDeckRequest(
                newName = "Imported Deck",
                newDescription = "Imported Description",
            )

        val createdDeckResponse =
            DeckResponse(
                id = newDeckId,
                name = importRequest.newName,
                description = importRequest.newDescription,
                cardCount = 0,
                learnedPercent = 0,
                lastStudied = null,
                createdAt = OffsetDateTime.now().toString(),
                updatedAt = OffsetDateTime.now().toString(),
            )

        val persistedNewDeck =
            deck.copy(
                id = newDeckId,
                userId = newUserId,
                name = importRequest.newName,
                description = importRequest.newDescription,
                cardsCount = 0,
            )

        whenever(shareRepository.findByToken(token)).thenReturn(deckId)
        whenever(deckRepository.findById(deckId)).thenReturn(deck)

        whenever(
            deckService.createDeck(
                eq(newUserId),
                eq(CreateDeckRequest(name = importRequest.newName, description = importRequest.newDescription)),
            ),
        ).thenReturn(createdDeckResponse)

        whenever(cardRepository.copyAllToDeck(sourceDeckId = deckId, targetDeckId = newDeckId)).thenReturn(5)

        whenever(deckRepository.findById(newDeckId)).thenReturn(persistedNewDeck)
        whenever(deckRepository.update(any())).thenAnswer { it.getArgument<Deck>(0) }

        val result = deckShareService.importSharedDeck(token, newUserId, importRequest)

        assertThat(result.name).isEqualTo("Imported Deck")
        assertThat(result.description).isEqualTo("Imported Description")
        assertThat(result.cardCount).isEqualTo(5)

        verify(shareRepository).findByToken(token)
        verify(deckRepository).findById(deckId)
        verify(deckService).createDeck(
            newUserId,
            CreateDeckRequest(name = "Imported Deck", description = "Imported Description"),
        )
        verify(cardRepository).copyAllToDeck(sourceDeckId = deckId, targetDeckId = newDeckId)
        verify(deckRepository).findById(newDeckId)
        verify(deckRepository).update(any())
    }

    @Test
    fun importSharedDeck_shouldThrowNotFoundException_whenTokenNotExists() {
        whenever(shareRepository.findByToken(token)).thenReturn(null)

        assertThatThrownBy { deckShareService.importSharedDeck(token, userId, null) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.TOKEN_NOT_FOUND)

        verify(deckService, never()).createDeck(any(), any())
    }
}
