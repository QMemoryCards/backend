package ru.spbstu.memory.cards.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.spbstu.memory.cards.BaseIntegrationTest
import ru.spbstu.memory.cards.dto.request.StudyAnswerRequest
import ru.spbstu.memory.cards.dto.request.StudyStatusEnum

class StudyIntegrationTest : BaseIntegrationTest() {
    @Test
    @DisplayName("10.1 Старт режима изучения")
    fun getCardsForStudy_shouldReturnAllDeckCards() {
        val userId = createUser(email = "study@mail.com", login = "study_user")
        val deckId = createDeck(userId = userId, name = "Study deck", description = "desc")
        createCard(deckId, question = "Q1", answer = "A1")
        createCard(deckId, question = "Q2", answer = "A2")
        createCard(deckId, question = "Q3", answer = "A3")

        val deck = deckRepository.findById(deckId)!!
        deckRepository.update(deck.copy(cardsCount = 3))

        val session = loginUser(login = "study_user")

        mockMvc.perform(get("/api/v1/study/$deckId/cards").session(session))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
    }

    @DisplayName("10.2 Отправка ответа remembered в режиме изучения")
    @Test
    fun processStudyAnswer_shouldMarkCardAsRememberedAndUpdateDeckProgress() {
        val userId = createUser(email = "study@mail.com", login = "study_user")
        val deckId = createDeck(userId = userId, name = "Study deck", description = "desc")
        val card1Id = createCard(deckId, "Q1", "A1")
        createCard(deckId, "Q2", "A2")

        val deck = deckRepository.findById(deckId)!!
        deckRepository.update(deck.copy(cardsCount = 2, learnedPercent = 0))

        val session = loginUser(login = "study_user")

        mockMvc.perform(
            post("/api/v1/study/$deckId/answer")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        StudyAnswerRequest(
                            cardId = card1Id,
                            status = StudyStatusEnum.REMEMBERED,
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.learnedPercent").value(50))

        val updatedCard = cardRepository.findById(card1Id)!!
        assertThat(updatedCard.isLearned).isTrue()

        val updatedDeck = deckRepository.findById(deckId)!!
        assertThat(updatedDeck.learnedPercent).isEqualTo(50)
        assertThat(updatedDeck.lastStudied).isNotNull
    }

    @DisplayName("10.2 Отправка ответа forgotten в режиме изучения")
    @Test
    fun processStudyAnswer_shouldMarkCardAsForgotten() {
        val userId = createUser(email = "study@mail.com", login = "study_user")
        val deckId = createDeck(userId = userId, name = "Study deck", description = "desc")
        val cardId = createCard(deckId = deckId, question = "Q1", answer = "A1")

        val card = cardRepository.findById(cardId)!!
        cardRepository.update(card.copy(isLearned = true))

        val deck = deckRepository.findById(deckId)!!
        deckRepository.update(deck.copy(cardsCount = 1, learnedPercent = 100))

        val session = loginUser(login = "study_user")

        mockMvc.perform(
            post("/api/v1/study/$deckId/answer")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        StudyAnswerRequest(
                            cardId = cardId,
                            status = StudyStatusEnum.FORGOTTEN,
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.learnedPercent").value(0))

        val updatedCard = cardRepository.findById(cardId)!!
        assertThat(updatedCard.isLearned).isFalse()

        val updatedDeck = deckRepository.findById(deckId)!!
        assertThat(updatedDeck.learnedPercent).isEqualTo(0)
        assertThat(updatedDeck.lastStudied).isNotNull
    }
}
