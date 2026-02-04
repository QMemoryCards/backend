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
import ru.spbstu.memory.cards.dto.request.CreateDeckRequest
import ru.spbstu.memory.cards.dto.response.DeckResponse
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.persistence.model.PaginatedResult
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.deck.DeckService
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class DeckControllerTest(
    @Mock private val deckService: DeckService,
) {
    private val controller = DeckController(deckService)

    private val userId = UUID.randomUUID()
    private val deckId = UUID.randomUUID()

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
    fun getDecks_shouldReturnPaginatedDecks_whenAuthenticated() {
        val deckResponse =
            DeckResponse(
                id = deckId,
                name = "Test Deck",
                description = "Test Description",
                cardCount = 5,
                learnedPercent = 0,
                lastStudied = null,
                createdAt = OffsetDateTime.now().toString(),
                updatedAt = OffsetDateTime.now().toString(),
            )

        whenever(deckService.getDecksPaginated(eq(userId), eq(0), eq(20)))
            .thenReturn(PaginatedResult(listOf(deckResponse), 1L))

        val result = controller.getDecks(principal, 0, 20)

        assertThat(result.content).hasSize(1)
        assertThat(result.content.single().name).isEqualTo("Test Deck")
        assertThat(result.totalElements).isEqualTo(1L)
        assertThat(result.totalPages).isEqualTo(1)
        assertThat(result.page).isEqualTo(0)
        assertThat(result.size).isEqualTo(20)

        verify(deckService).getDecksPaginated(userId, 0, 20)
    }

    @Test
    fun createDeck_shouldReturnCreatedDeck_whenAuthenticated() {
        val request =
            CreateDeckRequest(
                name = "New Deck",
                description = "New Description",
            )
        val response =
            DeckResponse(
                id = deckId,
                name = "New Deck",
                description = "New Description",
                cardCount = 0,
                learnedPercent = 0,
                lastStudied = null,
                createdAt = OffsetDateTime.now().toString(),
                updatedAt = OffsetDateTime.now().toString(),
            )

        whenever(deckService.createDeck(eq(userId), eq(request))).thenReturn(response)

        val result = controller.createDeck(principal, request)

        assertThat(result.name).isEqualTo("New Deck")
        verify(deckService).createDeck(userId, request)
    }

    @Test
    fun getDeck_shouldReturnDeck_whenAuthenticated() {
        val response =
            DeckResponse(
                id = deckId,
                name = "Test Deck",
                description = "Test Description",
                cardCount = 5,
                learnedPercent = 0,
                lastStudied = null,
                createdAt = OffsetDateTime.now().toString(),
                updatedAt = OffsetDateTime.now().toString(),
            )

        whenever(deckService.getDeck(eq(deckId), eq(userId))).thenReturn(response)

        val result = controller.getDeck(principal, deckId)

        assertThat(result.id).isEqualTo(deckId)
        verify(deckService).getDeck(deckId, userId)
    }

    @Test
    fun updateDeck_shouldReturnUpdatedDeck_whenAuthenticated() {
        val request =
            CreateDeckRequest(
                name = "Updated Deck",
                description = "Updated Description",
            )
        val response =
            DeckResponse(
                id = deckId,
                name = "Updated Deck",
                description = "Updated Description",
                cardCount = 5,
                learnedPercent = 0,
                lastStudied = null,
                createdAt = OffsetDateTime.now().toString(),
                updatedAt = OffsetDateTime.now().toString(),
            )

        whenever(deckService.updateDeck(eq(deckId), eq(userId), eq(request))).thenReturn(response)

        val result = controller.updateDeck(principal, deckId, request)

        assertThat(result.name).isEqualTo("Updated Deck")
        verify(deckService).updateDeck(deckId, userId, request)
    }

    @Test
    fun deleteDeck_shouldReturnOk_whenAuthenticated() {
        controller.deleteDeck(principal, deckId)

        verify(deckService).deleteDeck(deckId, userId)
    }

    @Test
    fun getDecks_shouldThrowUnauthorized_whenPrincipalIsNull() {
        assertThatThrownBy { controller.getDecks(null, 0, 20) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun createDeck_shouldThrowUnauthorized_whenPrincipalIsNull() {
        val request =
            CreateDeckRequest(
                name = "New Deck",
                description = "New Description",
            )

        assertThatThrownBy { controller.createDeck(null, request) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun getDeck_shouldThrowUnauthorized_whenPrincipalIsNull() {
        assertThatThrownBy { controller.getDeck(null, deckId) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun updateDeck_shouldThrowUnauthorized_whenPrincipalIsNull() {
        val request =
            CreateDeckRequest(
                name = "Updated Deck",
                description = "Updated Description",
            )

        assertThatThrownBy { controller.updateDeck(null, deckId, request) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun deleteDeck_shouldThrowUnauthorized_whenPrincipalIsNull() {
        assertThatThrownBy { controller.deleteDeck(null, deckId) }
            .isInstanceOf(UnauthorizedException::class.java)
    }
}
