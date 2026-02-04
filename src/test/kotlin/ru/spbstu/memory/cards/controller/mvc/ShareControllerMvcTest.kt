package ru.spbstu.memory.cards.controller.mvc

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.spbstu.memory.cards.controller.ShareController
import ru.spbstu.memory.cards.dto.request.ImportSharedDeckRequest
import ru.spbstu.memory.cards.dto.response.DeckResponse
import ru.spbstu.memory.cards.dto.response.DeckShareResponse
import ru.spbstu.memory.cards.dto.response.ShareResponse
import ru.spbstu.memory.cards.persistence.model.User
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.share.DeckShareService
import java.time.OffsetDateTime
import java.util.UUID

@WebMvcTest(ShareController::class)
class ShareControllerMvcTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockitoBean
    private lateinit var shareService: DeckShareService

    private val userId = UUID.randomUUID()
    private val deckId = UUID.randomUUID()
    private val token = UUID.randomUUID()

    private fun userDetails(): AppUserDetails =
        AppUserDetails(
            User(
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
        val resp = ShareResponse(token = token.toString(), url = "/api/v1/share/$token")
        whenever(shareService.generateShareToken(eq(deckId), eq(userId))).thenReturn(resp)

        mockMvc.perform(
            post("/api/v1/decks/$deckId/share")
                .with(user(userDetails()))
                .with(csrf()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value(token.toString()))
            .andExpect(jsonPath("$.url").value("/api/v1/share/$token"))

        verify(shareService).generateShareToken(deckId, userId)
    }

    @Test
    fun shareDeck_shouldReturnUnauthorized_whenNotAuthenticated() {
        mockMvc.perform(
            post("/api/v1/decks/$deckId/share")
                .with(csrf()),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun getSharedDeck_shouldReturnDeckShareInfo_whenAuthenticated() {
        val resp = DeckShareResponse(name = "Shared Deck", description = "Description", cardCount = 10)
        whenever(shareService.getSharedDeck(eq(token))).thenReturn(resp)

        mockMvc.perform(
            get("/api/v1/share/$token")
                .with(user(userDetails())),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Shared Deck"))
            .andExpect(jsonPath("$.cardCount").value(10))

        verify(shareService).getSharedDeck(token)
    }

    @Test
    fun getSharedDeck_shouldReturnUnauthorized_whenNotAuthenticated() {
        mockMvc.perform(get("/api/v1/share/$token"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun importDeck_shouldReturnDeck_whenAuthenticatedWithCustomName() {
        val req = ImportSharedDeckRequest(newName = "My Copy", newDescription = "Desc")

        val deckResponse =
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

        whenever(shareService.importSharedDeck(eq(token), eq(userId), eq(req))).thenReturn(deckResponse)

        mockMvc.perform(
            post("/api/v1/share/$token/import")
                .with(user(userDetails()))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("My Copy"))

        verify(shareService).importSharedDeck(token, userId, req)
    }

    @Test
    fun importDeck_shouldReturnDeck_whenAuthenticatedWithNullRequestBody() {
        val deckResponse =
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

        whenever(shareService.importSharedDeck(eq(token), eq(userId), eq(null))).thenReturn(deckResponse)

        mockMvc.perform(
            post("/api/v1/share/$token/import")
                .with(user(userDetails()))
                .with(csrf()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Original"))

        verify(shareService).importSharedDeck(token, userId, null)
    }

    @Test
    fun importDeck_shouldReturnUnauthorized_whenNotAuthenticated() {
        mockMvc.perform(
            post("/api/v1/share/$token/import")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"),
        )
            .andExpect(status().isUnauthorized)
    }
}
