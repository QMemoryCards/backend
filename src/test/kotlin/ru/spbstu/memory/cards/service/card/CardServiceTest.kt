package ru.spbstu.memory.cards.service.card

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
import ru.spbstu.memory.cards.dto.request.CreateCardRequest
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.LimitExceededException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.persistence.CardRepository
import ru.spbstu.memory.cards.persistence.DeckRepository
import ru.spbstu.memory.cards.config.TxRunner
import ru.spbstu.memory.cards.persistence.model.Card
import ru.spbstu.memory.cards.persistence.model.Deck
import ru.spbstu.memory.cards.persistence.model.PaginatedResult
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CardServiceTest(
    @Mock private val cardRepository: CardRepository,
    @Mock private val deckRepository: DeckRepository,
) {
    private val txRunner =
        object : TxRunner {
            override fun <T> required(block: () -> T): T = block()
        }

    private val cardService = CardService(cardRepository, deckRepository, txRunner)

    private val userId = UUID.randomUUID()
    private val deckId = UUID.randomUUID()
    private val cardId = UUID.randomUUID()

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

    private val card =
        Card(
            id = cardId,
            deckId = deckId,
            question = "Question",
            answer = "Answer",
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            isLearned = false,
        )

    private val createRequest =
        CreateCardRequest(
            question = "New Question",
            answer = "New Answer",
        )

    @Test
    fun getCards_shouldReturnPaginatedCards_whenUserOwnsDeck() {
        val page = 0
        val size = 20
        val paginatedResult = PaginatedResult(listOf(card), 1L)

        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findAllByDeckIdPaginated(deckId, page, size)).thenReturn(paginatedResult)

        val result = cardService.getCards(deckId, userId, page, size)

        assertThat(result.items).hasSize(1)
        assertThat(result.total).isEqualTo(1L)
        verify(deckRepository).findById(deckId)
        verify(cardRepository).findAllByDeckIdPaginated(deckId, page, size)
    }

    @Test
    fun getCards_shouldThrowNotFoundException_whenDeckNotExists() {
        whenever(deckRepository.findById(deckId)).thenReturn(null)

        assertThatThrownBy { cardService.getCards(deckId, userId, 0, 20) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.DECK_NOT_FOUND)
    }

    @Test
    fun getCards_shouldThrowForbiddenException_whenUserNotOwnsDeck() {
        val otherUserId = UUID.randomUUID()
        whenever(deckRepository.findById(deckId)).thenReturn(deck)

        assertThatThrownBy { cardService.getCards(deckId, otherUserId, 0, 20) }
            .isInstanceOf(ForbiddenException::class.java)
    }

    @Test
    fun createCard_shouldCreateCard_whenLimitNotExceededAndUserOwnsDeck() {
        val deckWithSpace = deck.copy(cardsCount = CardService.CARD_LIMIT - 1)
        whenever(deckRepository.findById(deckId)).thenReturn(deckWithSpace)
        whenever(cardRepository.saveNew(deckId, createRequest.question, createRequest.answer)).thenReturn(card)
        whenever(deckRepository.update(any())).thenAnswer { it.getArgument<Deck>(0) }

        val result = cardService.createCard(deckId, userId, createRequest)

        assertThat(result.question).isEqualTo(card.question)
        verify(deckRepository).findById(deckId)
        verify(cardRepository).saveNew(deckId, createRequest.question, createRequest.answer)
        verify(deckRepository).update(any())
    }

    @Test
    fun createCard_shouldThrowNotFoundException_whenDeckNotExists() {
        whenever(deckRepository.findById(deckId)).thenReturn(null)

        assertThatThrownBy { cardService.createCard(deckId, userId, createRequest) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.DECK_NOT_FOUND)

        verify(cardRepository, never()).saveNew(any(), any(), any())
    }

    @Test
    fun createCard_shouldThrowLimitExceededException_whenCardLimitReached() {
        val deckAtLimit = deck.copy(cardsCount = CardService.CARD_LIMIT)
        whenever(deckRepository.findById(deckId)).thenReturn(deckAtLimit)

        assertThatThrownBy { cardService.createCard(deckId, userId, createRequest) }
            .isInstanceOf(LimitExceededException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.CARD_LIMIT)

        verify(cardRepository, never()).saveNew(any(), any(), any())
    }

    @Test
    fun updateCard_shouldUpdateCard_whenUserOwnsDeck() {
        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findById(cardId)).thenReturn(card)
        whenever(cardRepository.update(any())).thenAnswer { inv ->
            inv.getArgument<Card>(0).copy(updatedAt = OffsetDateTime.now())
        }

        val result = cardService.updateCard(deckId, cardId, userId, createRequest)

        assertThat(result.question).isEqualTo(createRequest.question)
        assertThat(result.answer).isEqualTo(createRequest.answer)
        verify(cardRepository).update(any())
    }

    @Test
    fun updateCard_shouldThrowNotFoundException_whenCardNotExists() {
        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findById(cardId)).thenReturn(null)

        assertThatThrownBy { cardService.updateCard(deckId, cardId, userId, createRequest) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.CARD_NOT_FOUND)

        verify(cardRepository, never()).update(any())
    }

    @Test
    fun deleteCard_shouldDeleteCard_whenUserOwnsDeck() {
        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findById(cardId)).thenReturn(card)
        whenever(deckRepository.update(any())).thenAnswer { it.getArgument<Deck>(0) }

        cardService.deleteCard(deckId, cardId, userId)

        verify(cardRepository).delete(cardId)
        verify(deckRepository).update(any())
    }

    @Test
    fun deleteCard_shouldThrowNotFoundException_whenDeckNotExists() {
        whenever(deckRepository.findById(deckId)).thenReturn(null)

        assertThatThrownBy { cardService.deleteCard(deckId, cardId, userId) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.DECK_NOT_FOUND)

        verify(cardRepository, never()).delete(any())
    }

    @Test
    fun getAllCards_shouldReturnCards_whenUserOwnsDeck() {
        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findAllByDeckId(deckId)).thenReturn(listOf(card))

        val result = cardService.getAllCards(deckId, userId)

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(cardId)

        verify(cardRepository).findAllByDeckId(deckId)
    }

    @Test
    fun getAllCards_shouldThrowNotFoundException_whenDeckNotExists() {
        whenever(deckRepository.findById(deckId)).thenReturn(null)

        assertThatThrownBy { cardService.getAllCards(deckId, userId) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.DECK_NOT_FOUND)
    }

    @Test
    fun getAllCards_shouldThrowForbiddenException_whenUserNotOwnsDeck() {
        val otherUserId = UUID.randomUUID()
        whenever(deckRepository.findById(deckId)).thenReturn(deck)

        assertThatThrownBy { cardService.getAllCards(deckId, otherUserId) }
            .isInstanceOf(ForbiddenException::class.java)
    }

    @Test
    fun createCard_shouldThrowForbiddenException_whenUserNotOwnsDeck() {
        val otherUserId = UUID.randomUUID()
        whenever(deckRepository.findById(deckId)).thenReturn(deck)

        assertThatThrownBy { cardService.createCard(deckId, otherUserId, createRequest) }
            .isInstanceOf(ForbiddenException::class.java)

        verify(cardRepository, never()).saveNew(any(), any(), any())
        verify(deckRepository, never()).update(any())
    }

    @Test
    fun updateCard_shouldThrowNotFoundException_whenDeckNotExists() {
        whenever(deckRepository.findById(deckId)).thenReturn(null)

        assertThatThrownBy { cardService.updateCard(deckId, cardId, userId, createRequest) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.DECK_NOT_FOUND)

        verify(cardRepository, never()).update(any())
    }

    @Test
    fun updateCard_shouldThrowForbiddenException_whenUserNotOwnsDeck() {
        val otherUserId = UUID.randomUUID()
        whenever(deckRepository.findById(deckId)).thenReturn(deck)

        assertThatThrownBy { cardService.updateCard(deckId, cardId, otherUserId, createRequest) }
            .isInstanceOf(ForbiddenException::class.java)

        verify(cardRepository, never()).update(any())
    }

    @Test
    fun updateCard_shouldThrowNotFoundException_whenCardBelongsToAnotherDeck() {
        val otherDeckId = UUID.randomUUID()
        val cardFromOtherDeck = card.copy(deckId = otherDeckId)

        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findById(cardId)).thenReturn(cardFromOtherDeck)

        assertThatThrownBy { cardService.updateCard(deckId, cardId, userId, createRequest) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.CARD_NOT_FOUND)

        verify(cardRepository, never()).update(any())
    }

    @Test
    fun deleteCard_shouldThrowNotFoundException_whenCardNotExists() {
        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findById(cardId)).thenReturn(null)

        assertThatThrownBy { cardService.deleteCard(deckId, cardId, userId) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.CARD_NOT_FOUND)

        verify(cardRepository, never()).delete(any())
        verify(deckRepository, never()).update(any())
    }

    @Test
    fun deleteCard_shouldThrowNotFoundException_whenCardBelongsToAnotherDeck() {
        val otherDeckId = UUID.randomUUID()
        val cardFromOtherDeck = card.copy(deckId = otherDeckId)

        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findById(cardId)).thenReturn(cardFromOtherDeck)

        assertThatThrownBy { cardService.deleteCard(deckId, cardId, userId) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.CARD_NOT_FOUND)

        verify(cardRepository, never()).delete(any())
        verify(deckRepository, never()).update(any())
    }

    @Test
    fun deleteCard_shouldKeepZeroCount_whenDeckHasZeroCards() {
        val emptyDeck = deck.copy(cardsCount = 0)

        whenever(deckRepository.findById(deckId)).thenReturn(emptyDeck)
        whenever(cardRepository.findById(cardId)).thenReturn(card)
        whenever(deckRepository.update(any())).thenAnswer { it.getArgument<Deck>(0) }

        cardService.deleteCard(deckId, cardId, userId)

        verify(cardRepository).delete(cardId)
        verify(deckRepository).update(
            eq(emptyDeck.copy(cardsCount = 0))
        )
    }
}


