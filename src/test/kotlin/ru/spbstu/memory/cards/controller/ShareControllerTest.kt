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
import ru.spbstu.memory.cards.dto.request.ImportSharedDeckRequest
import ru.spbstu.memory.cards.dto.response.DeckResponse
import ru.spbstu.memory.cards.dto.response.DeckShareResponse
import ru.spbstu.memory.cards.dto.response.ShareResponse
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.share.DeckShareService
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ShareControllerTest(
    @Mock private val shareService: DeckShareService,
) {
    private val controller = ShareController(shareService)

    private val userId = UUID.randomUUID()
    private val deckId = UUID.randomUUID()
    private val token = UUID.randomUUID()

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
    fun shareDeck_shouldReturnShareResponse_whenAuthenticated() {
        val response = ShareResponse(token = token.toString(), url = "/api/v1/share/$token")
        whenever(shareService.generateShareToken(eq(deckId), eq(userId))).thenReturn(response)

        val result = controller.shareDeck(principal, deckId)

        assertThat(result.token).isEqualTo(token.toString())
        assertThat(result.url).isEqualTo("/api/v1/share/$token")
        verify(shareService).generateShareToken(deckId, userId)
    }

    @Test
    fun shareDeck_shouldThrowUnauthorized_whenNotAuthenticated() {
        assertThatThrownBy { controller.shareDeck(null, deckId) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun getSharedDeck_shouldReturnDeckShareInfo_whenAuthenticated() {
        val response = DeckShareResponse(name = "Shared Deck", description = "Description", cardCount = 10)
        whenever(shareService.getSharedDeck(eq(token))).thenReturn(response)

        val result = controller.getSharedDeck(principal, token)

        assertThat(result.name).isEqualTo("Shared Deck")
        assertThat(result.cardCount).isEqualTo(10)
        verify(shareService).getSharedDeck(token)
    }

    @Test
    fun getSharedDeck_shouldThrowUnauthorized_whenNotAuthenticated() {
        assertThatThrownBy { controller.getSharedDeck(null, token) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun importDeck_shouldReturnDeck_whenAuthenticatedWithCustomName() {
        val req = ImportSharedDeckRequest(newName = "My Copy", newDescription = "Desc")
        val response =
            DeckResponse(
                id = UUID.randomUUID(),
                name = "My Copy",
                description = "Desc",
                cardCount = 10,
                learnedPercent = 0,
                lastStudied = null,
                createdAt = OffsetDateTime.now().toString(),
                updatedAt = OffsetDateTime.now().toString(),
            )

        whenever(shareService.importSharedDeck(eq(token), eq(userId), eq(req))).thenReturn(response)

        val result = controller.importDeck(principal, token, req)

        assertThat(result.name).isEqualTo("My Copy")
        verify(shareService).importSharedDeck(token, userId, req)
    }

    @Test
    fun importDeck_shouldReturnDeck_whenAuthenticatedWithNullRequestBody() {
        val response =
            DeckResponse(
                id = UUID.randomUUID(),
                name = "Original",
                description = null,
                cardCount = 5,
                learnedPercent = 0,
                lastStudied = null,
                createdAt = OffsetDateTime.now().toString(),
                updatedAt = OffsetDateTime.now().toString(),
            )

        whenever(shareService.importSharedDeck(eq(token), eq(userId), eq(null))).thenReturn(response)

        val result = controller.importDeck(principal, token, null)

        assertThat(result.name).isEqualTo("Original")
        verify(shareService).importSharedDeck(token, userId, null)
    }

    @Test
    fun importDeck_shouldThrowUnauthorized_whenNotAuthenticated() {
        assertThatThrownBy { controller.importDeck(null, token, null) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun shareDeck_shouldThrowUnauthorized_whenPrincipalIsNull() {
        assertThatThrownBy { controller.shareDeck(null, deckId) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun getSharedDeck_shouldThrowUnauthorized_whenPrincipalIsNull() {
        assertThatThrownBy { controller.getSharedDeck(null, token) }
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun importDeck_shouldThrowUnauthorized_whenPrincipalIsNull() {
        assertThatThrownBy { controller.importDeck(null, token, null) }
            .isInstanceOf(UnauthorizedException::class.java)
    }
}
