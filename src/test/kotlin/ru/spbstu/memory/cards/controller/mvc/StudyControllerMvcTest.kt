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
import ru.spbstu.memory.cards.controller.StudyController
import ru.spbstu.memory.cards.dto.request.StudyAnswerRequest
import ru.spbstu.memory.cards.dto.request.StudyStatusEnum
import ru.spbstu.memory.cards.dto.response.CardResponse
import ru.spbstu.memory.cards.dto.response.StudyAnswerResponse
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.card.CardService
import ru.spbstu.memory.cards.service.study.StudyService
import java.time.OffsetDateTime
import java.util.UUID

@WebMvcTest(StudyController::class)
class StudyControllerMvcTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockitoBean
    private lateinit var cardService: CardService

    @MockitoBean
    private lateinit var studyService: StudyService

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
    fun getCardsForStudy_shouldReturnCards_whenAuthenticated() {
        val card =
            CardResponse(
                id = cardId,
                question = "Question",
                answer = "Answer",
                isLearned = false,
            )

        whenever(cardService.getAllCards(eq(deckId), eq(userId))).thenReturn(listOf(card))

        mockMvc.perform(
            get("/api/v1/study/$deckId/cards")
                .with(user(userDetails())),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].question").value("Question"))

        verify(cardService).getAllCards(deckId, userId)
    }

    @Test
    fun processCardAnswer_shouldReturnStudyAnswerResponse_whenAuthenticated() {
        val req =
            StudyAnswerRequest(
                cardId = cardId,
                status = StudyStatusEnum.REMEMBERED,
            )
        val resp = StudyAnswerResponse(learnedPercent = 50)

        whenever(studyService.processStudyAnswer(eq(deckId), eq(userId), eq(req))).thenReturn(resp)

        mockMvc.perform(
            post("/api/v1/study/$deckId/answer")
                .with(user(userDetails()))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.learnedPercent").value(50))

        verify(studyService).processStudyAnswer(deckId, userId, req)
    }
}
