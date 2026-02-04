package ru.spbstu.memory.cards.controller.mvc

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doNothing
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.spbstu.memory.cards.controller.DeckController
import ru.spbstu.memory.cards.dto.request.CreateDeckRequest
import ru.spbstu.memory.cards.dto.response.DeckResponse
import ru.spbstu.memory.cards.persistence.model.PaginatedResult
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.deck.DeckService
import java.time.OffsetDateTime
import java.util.UUID

@WebMvcTest(DeckController::class)
class DeckControllerMvcTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockitoBean
    private lateinit var deckService: DeckService

    private val userId = UUID.randomUUID()
    private val deckId = UUID.randomUUID()

    private fun userDetails(): AppUserDetails =
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

        mockMvc.perform(
            get("/api/v1/decks")
                .with(user(userDetails()))
                .param("page", "0")
                .param("size", "20"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].id").value(deckId.toString()))
            .andExpect(jsonPath("$.content[0].name").value("Test Deck"))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))

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

        mockMvc.perform(
            post("/api/v1/decks")
                .with(user(userDetails()))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(deckId.toString()))
            .andExpect(jsonPath("$.name").value("New Deck"))
            .andExpect(jsonPath("$.description").value("New Description"))

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

        mockMvc.perform(
            get("/api/v1/decks/$deckId")
                .with(user(userDetails())),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(deckId.toString()))
            .andExpect(jsonPath("$.name").value("Test Deck"))

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

        mockMvc.perform(
            put("/api/v1/decks/$deckId")
                .with(user(userDetails()))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(deckId.toString()))
            .andExpect(jsonPath("$.name").value("Updated Deck"))
            .andExpect(jsonPath("$.description").value("Updated Description"))

        verify(deckService).updateDeck(deckId, userId, request)
    }

    @Test
    fun deleteDeck_shouldReturnOk_whenAuthenticated() {
        doNothing().whenever(deckService).deleteDeck(eq(deckId), eq(userId))

        mockMvc.perform(
            delete("/api/v1/decks/$deckId")
                .with(user(userDetails()))
                .with(csrf()),
        )
            .andExpect(status().isOk)

        verify(deckService).deleteDeck(deckId, userId)
    }
}
