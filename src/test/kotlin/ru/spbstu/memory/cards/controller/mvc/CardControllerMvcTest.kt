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
import ru.spbstu.memory.cards.controller.CardController
import ru.spbstu.memory.cards.dto.request.CreateCardRequest
import ru.spbstu.memory.cards.dto.response.CardResponse
import ru.spbstu.memory.cards.persistence.model.PaginatedResult
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.card.CardService
import java.time.OffsetDateTime
import java.util.UUID

@WebMvcTest(CardController::class)
class CardControllerMvcTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockitoBean
    private lateinit var cardService: CardService

    private val userId = UUID.randomUUID()
    private val deckId = UUID.randomUUID()
    private val cardId = UUID.randomUUID()

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
    fun getCards_shouldReturnPaginatedCards_whenAuthenticated() {
        val cardResponse =
            CardResponse(
                id = cardId,
                question = "Question",
                answer = "Answer",
                isLearned = false,
            )
        val paginated = PaginatedResult(listOf(cardResponse), 1L)

        whenever(cardService.getCards(eq(deckId), eq(userId), eq(0), eq(20))).thenReturn(paginated)

        mockMvc.perform(
            get("/api/v1/decks/$deckId/cards")
                .with(user(userDetails()))
                .with(csrf())
                .param("page", "0")
                .param("size", "20"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].id").value(cardId.toString()))
            .andExpect(jsonPath("$.content[0].question").value("Question"))
            .andExpect(jsonPath("$.content[0].answer").value("Answer"))
            .andExpect(jsonPath("$.content[0].isLearned").value(false))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))

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

        mockMvc.perform(
            post("/api/v1/decks/$deckId/cards")
                .with(user(userDetails()))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(cardId.toString()))
            .andExpect(jsonPath("$.question").value("New Question"))
            .andExpect(jsonPath("$.answer").value("New Answer"))
            .andExpect(jsonPath("$.isLearned").value(false))

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

        mockMvc.perform(
            put("/api/v1/decks/$deckId/cards/$cardId")
                .with(user(userDetails()))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(cardId.toString()))
            .andExpect(jsonPath("$.question").value("Updated Question"))
            .andExpect(jsonPath("$.answer").value("Updated Answer"))
            .andExpect(jsonPath("$.isLearned").value(false))

        verify(cardService).updateCard(deckId, cardId, userId, request)
    }

    @Test
    fun deleteCard_shouldReturnOk_whenAuthenticated() {
        doNothing().whenever(cardService).deleteCard(eq(deckId), eq(cardId), eq(userId))

        mockMvc.perform(
            delete("/api/v1/decks/$deckId/cards/$cardId")
                .with(csrf())
                .with(user(userDetails())),
        )
            .andExpect(status().isOk)

        verify(cardService).deleteCard(deckId, cardId, userId)
    }
}
