package ru.spbstu.memory.cards.service.study

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
import ru.spbstu.memory.cards.config.TxRunner
import ru.spbstu.memory.cards.dto.request.StudyAnswerRequest
import ru.spbstu.memory.cards.dto.request.StudyStatusEnum
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.persistence.CardRepository
import ru.spbstu.memory.cards.persistence.DeckRepository
import ru.spbstu.memory.cards.persistence.model.Card
import ru.spbstu.memory.cards.persistence.model.Deck
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class StudyServiceTest(
    @Mock private val cardRepository: CardRepository,
    @Mock private val deckRepository: DeckRepository,
) {
    private val txRunner =
        object : TxRunner {
            override fun <T> required(block: () -> T): T = block()
        }

    private val studyService = StudyService(cardRepository, deckRepository, txRunner)

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
            cardsCount = 10,
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
    private val studyRequest =
        StudyAnswerRequest(
            cardId = cardId,
            status = StudyStatusEnum.REMEMBERED,
        )

    @Test
    fun processStudyAnswer_shouldUpdateCardAndDeck_whenUserOwnsDeck() {
        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findById(cardId)).thenReturn(card)
        whenever(cardRepository.getLearnedCount(deckId)).thenReturn(5L)
        whenever(cardRepository.update(any())).thenAnswer { invocation ->
            invocation.getArgument<Card>(0)
        }
        whenever(deckRepository.update(any())).thenAnswer { invocation ->
            invocation.getArgument<Deck>(0)
        }

        val result = studyService.processStudyAnswer(deckId, userId, studyRequest)

        assertThat(result.learnedPercent).isEqualTo(50)
        verify(cardRepository).update(any())
        verify(deckRepository).update(any())
    }

    @Test
    fun processStudyAnswer_shouldCalculateLearnedPercentCorrectly() {
        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findById(cardId)).thenReturn(card)
        whenever(cardRepository.getLearnedCount(deckId)).thenReturn(7L)
        whenever(cardRepository.update(any())).thenAnswer { invocation ->
            invocation.getArgument<Card>(0)
        }
        whenever(deckRepository.update(any())).thenAnswer { invocation ->
            invocation.getArgument<Deck>(0)
        }

        val result = studyService.processStudyAnswer(deckId, userId, studyRequest)

        assertThat(result.learnedPercent).isEqualTo(70)
    }

    @Test
    fun processStudyAnswer_shouldThrowNotFoundException_whenDeckNotExists() {
        whenever(deckRepository.findById(deckId)).thenReturn(null)

        assertThatThrownBy { studyService.processStudyAnswer(deckId, userId, studyRequest) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.DECK_NOT_FOUND)

        verify(cardRepository, never()).update(any())
        verify(deckRepository, never()).update(any())
    }

    @Test
    fun processStudyAnswer_shouldThrowForbiddenException_whenUserNotOwnsDeck() {
        val otherUserId = UUID.randomUUID()
        whenever(deckRepository.findById(deckId)).thenReturn(deck)

        assertThatThrownBy { studyService.processStudyAnswer(deckId, otherUserId, studyRequest) }
            .isInstanceOf(ForbiddenException::class.java)

        verify(cardRepository, never()).update(any())
        verify(deckRepository, never()).update(any())
    }

    @Test
    fun processStudyAnswer_shouldThrowNotFoundException_whenCardNotExists() {
        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findById(cardId)).thenReturn(null)

        assertThatThrownBy { studyService.processStudyAnswer(deckId, userId, studyRequest) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.CARD_NOT_FOUND)

        verify(cardRepository, never()).update(any())
        verify(deckRepository, never()).update(any())
    }

    @Test
    fun processStudyAnswer_shouldThrowNotFoundException_whenCardBelongsToAnotherDeck() {
        val otherDeckId = UUID.randomUUID()
        val cardFromOtherDeck = card.copy(deckId = otherDeckId)

        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findById(cardId)).thenReturn(cardFromOtherDeck)

        assertThatThrownBy { studyService.processStudyAnswer(deckId, userId, studyRequest) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.CARD_NOT_FOUND)

        verify(cardRepository, never()).update(any())
        verify(deckRepository, never()).update(any())
    }

    @Test
    fun processStudyAnswer_shouldSetCardNotLearned_whenStatusIsNotRemembered() {
        val req =
            StudyAnswerRequest(
                cardId = cardId,
                status = StudyStatusEnum.FORGOTTEN,
            )

        whenever(deckRepository.findById(deckId)).thenReturn(deck)
        whenever(cardRepository.findById(cardId)).thenReturn(card.copy(isLearned = true))
        whenever(cardRepository.getLearnedCount(deckId)).thenReturn(0L)
        whenever(cardRepository.update(any())).thenAnswer { it.getArgument<Card>(0) }
        whenever(deckRepository.update(any())).thenAnswer { it.getArgument<Deck>(0) }

        val result = studyService.processStudyAnswer(deckId, userId, req)

        assertThat(result.learnedPercent).isEqualTo(0)

        verify(cardRepository).update(
            org.mockito.kotlin.check<Card> { updated ->
                assertThat(updated.isLearned).isFalse()
            },
        )
    }

    @Test
    fun processStudyAnswer_shouldReturnZeroPercent_whenDeckHasZeroCards() {
        val emptyDeck = deck.copy(cardsCount = 0)

        whenever(deckRepository.findById(deckId)).thenReturn(emptyDeck)
        whenever(cardRepository.findById(cardId)).thenReturn(card)
        whenever(cardRepository.getLearnedCount(deckId)).thenReturn(0L)
        whenever(cardRepository.update(any())).thenAnswer { it.getArgument<Card>(0) }
        whenever(deckRepository.update(any())).thenAnswer { it.getArgument<Deck>(0) }

        val result = studyService.processStudyAnswer(deckId, userId, studyRequest)

        assertThat(result.learnedPercent).isEqualTo(0)

        verify(deckRepository).update(
            org.mockito.kotlin.check<Deck> { updated ->
                assertThat(updated.learnedPercent).isEqualTo(0)
                assertThat(updated.lastStudied).isNotNull()
            },
        )
    }
}
