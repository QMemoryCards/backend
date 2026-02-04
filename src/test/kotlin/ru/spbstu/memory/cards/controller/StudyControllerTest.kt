package ru.spbstu.memory.cards.controller

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.spbstu.memory.cards.dto.request.StudyAnswerRequest
import ru.spbstu.memory.cards.dto.request.StudyStatusEnum
import ru.spbstu.memory.cards.dto.response.CardResponse
import ru.spbstu.memory.cards.dto.response.StudyAnswerResponse
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.card.CardService
import ru.spbstu.memory.cards.service.study.StudyService
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class StudyControllerTest(
    @Mock private val cardService: CardService,
    @Mock private val studyService: StudyService,
) {
    private val controller = StudyController(cardService, studyService)

    private val userId = UUID.randomUUID()
    private val deckId = UUID.randomUUID()
    private val cardId = UUID.randomUUID()

    private val principal =
        AppUserDetails(
            ru.spbstu.memory.cards.persistence.model.User(
                id = userId,
                email = "test@example.com",
                login = "testuser",
                passwordHash = "hash",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            ),
        )

    @Test
    fun getCardsForStudy_shouldReturnCards_whenAuthenticated() {
        val card =
            CardResponse(
                id = cardId,
                question = "Question",
                answer = "Answer",
                isLearned = false,
            )

        whenever(cardService.getAllCards(eq(deckId), eq(userId))).thenReturn(listOf(card))

        val result = controller.getCardsForStudy(principal, deckId)

        assertThat(result).hasSize(1)
        assertThat(result[0].question).isEqualTo("Question")
        verify(cardService).getAllCards(deckId, userId)
    }

    @Test
    fun getCardsForStudy_shouldThrowUnauthorized_whenNotAuthenticated() {
        assertThatThrownBy { controller.getCardsForStudy(null, deckId) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun processCardAnswer_shouldReturnStudyAnswerResponse_whenAuthenticated() {
        val req =
            StudyAnswerRequest(
                cardId = cardId,
                status = StudyStatusEnum.REMEMBERED,
            )
        val response = StudyAnswerResponse(learnedPercent = 50)

        whenever(studyService.processStudyAnswer(eq(deckId), eq(userId), eq(req))).thenReturn(response)

        val result = controller.processCardAnswer(principal, deckId, req)

        assertThat(result.learnedPercent).isEqualTo(50)
        verify(studyService).processStudyAnswer(deckId, userId, req)
    }

    @Test
    fun processCardAnswer_shouldThrowUnauthorized_whenNotAuthenticated() {
        val req =
            StudyAnswerRequest(
                cardId = cardId,
                status = StudyStatusEnum.REMEMBERED,
            )

        assertThatThrownBy { controller.processCardAnswer(null, deckId, req) }
            .isInstanceOf(UnauthorizedException::class.java)
    }
}
