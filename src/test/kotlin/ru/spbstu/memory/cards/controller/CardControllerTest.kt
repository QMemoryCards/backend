package ru.spbstu.memory.cards.controller

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.spbstu.memory.cards.dto.request.CreateCardRequest
import ru.spbstu.memory.cards.dto.response.CardResponse
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.persistence.model.PaginatedResult
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.card.CardService
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CardControllerTest(
    @Mock private val cardService: CardService,
) {
    private val controller = CardController(cardService)

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
    fun getCards_shouldReturnPaginatedCards_whenAuthenticated() {
        val cardResponse =
            CardResponse(
                id = cardId,
                question = "Question",
                answer = "Answer",
                isLearned = false,
            )

        whenever(cardService.getCards(eq(deckId), eq(userId), eq(0), eq(20)))
            .thenReturn(PaginatedResult(listOf(cardResponse), 1L))

        val result = controller.getCards(principal, deckId, 0, 20)

        assertThat(result.content).hasSize(1)
        assertThat(result.content.single().question).isEqualTo("Question")
        assertThat(result.totalElements).isEqualTo(1L)

        verify(cardService).getCards(deckId, userId, 0, 20)
    }

    @Test
    fun createCard_shouldReturnCreatedCard_whenAuthenticated() {
        val request =
            CreateCardRequest(
                question = "New Question",
                answer = "New Answer",
            )

        val response =
            CardResponse(
                id = cardId,
                question = "New Question",
                answer = "New Answer",
                isLearned = false,
            )

        whenever(cardService.createCard(eq(deckId), eq(userId), eq(request))).thenReturn(response)

        val result = controller.createCard(principal, deckId, request)

        assertThat(result.id).isEqualTo(cardId)
        assertThat(result.question).isEqualTo("New Question")

        verify(cardService).createCard(deckId, userId, request)
    }

    @Test
    fun updateCard_shouldReturnUpdatedCard_whenAuthenticated() {
        val request =
            CreateCardRequest(
                question = "Updated Question",
                answer = "Updated Answer",
            )

        val response =
            CardResponse(
                id = cardId,
                question = "Updated Question",
                answer = "Updated Answer",
                isLearned = false,
            )

        whenever(cardService.updateCard(eq(deckId), eq(cardId), eq(userId), eq(request))).thenReturn(response)

        val result = controller.updateCard(principal, deckId, cardId, request)

        assertThat(result.id).isEqualTo(cardId)
        assertThat(result.question).isEqualTo("Updated Question")

        verify(cardService).updateCard(deckId, cardId, userId, request)
    }

    @Test
    fun deleteCard_shouldReturnOk_whenAuthenticated() {
        doNothing().whenever(cardService).deleteCard(eq(deckId), eq(cardId), eq(userId))

        controller.deleteCard(principal, deckId, cardId)

        verify(cardService).deleteCard(deckId, cardId, userId)
    }

    @Test
    fun getCards_shouldThrowUnauthorized_whenPrincipalIsNull() {
        assertThatThrownBy { controller.getCards(null, deckId, 0, 20) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun createCard_shouldThrowUnauthorized_whenPrincipalIsNull() {
        val req = CreateCardRequest(question = "Q", answer = "A")
        assertThatThrownBy { controller.createCard(null, deckId, req) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun updateCard_shouldThrowUnauthorized_whenPrincipalIsNull() {
        val req = CreateCardRequest(question = "Q", answer = "A")

        assertThatThrownBy { controller.updateCard(null, deckId, cardId, req) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun deleteCard_shouldThrowUnauthorized_whenPrincipalIsNull() {
        assertThatThrownBy { controller.deleteCard(null, deckId, cardId) }
            .isInstanceOf(UnauthorizedException::class.java)
    }
}
