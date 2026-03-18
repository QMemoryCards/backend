package ru.spbstu.memory.cards.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.spbstu.memory.cards.BaseIntegrationTest
import ru.spbstu.memory.cards.dto.request.CreateCardRequest

class CardIntegrationTest : BaseIntegrationTest() {
    @Test
    @DisplayName("6.1 Добавление новой карточки")
    fun createCard_shouldCreateCardAndIncreaseDeckCardCount() {
        val userId = createUser(email = "cards@mail.com", login = "cards_user")
        val deckId = createDeck(userId = userId, name = "Deck", description = "Description")
        val session = loginUser(login = "cards_user")

        mockMvc.perform(
            post("/api/v1/decks/$deckId/cards")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        CreateCardRequest(
                            question = "Apple",
                            answer = "Яблоко",
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.question").value("Apple"))
            .andExpect(jsonPath("$.answer").value("Яблоко"))
            .andExpect(jsonPath("$.isLearned").value(false))

        val cards = cardRepository.findAllByDeckId(deckId)
        assertThat(cards).hasSize(1)
        assertThat(cards.first().question).isEqualTo("Apple")
        assertThat(cards.first().answer).isEqualTo("Яблоко")

        val deck = deckRepository.findById(deckId)!!
        assertThat(deck.cardsCount).isEqualTo(1)
    }

    @Test
    @DisplayName("7.1 Успешное удаление карточки")
    fun deleteCard_shouldDeleteCardAndDecreaseDeckCount() {
        val userId = createUser(email = "cards@mail.com", login = "cards_user")
        val deckId = createDeck(userId = userId, name = "Deck", description = "Description")
        val cardId = createCard(deckId = deckId, question = "Q1", answer = "A1")

        val deckBefore = deckRepository.findById(deckId)!!
        deckRepository.update(deckBefore.copy(cardsCount = 1))

        val session = loginUser(login = "cards_user")
        mockMvc.perform(delete("/api/v1/decks/$deckId/cards/$cardId").session(session))
            .andExpect(status().isOk)

        assertThat(cardRepository.findById(cardId)).isNull()

        val deckAfter = deckRepository.findById(deckId)!!
        assertThat(deckAfter.cardsCount).isEqualTo(0)
    }

    @Test
    @DisplayName("7.2 Удаление карточки из чужой колоды")
    fun deleteCard_shouldReturn403_whenDeletingCardFromForeignDeck() {
        createUser(email = "ua@mail.com", login = "user_a")
        val userBId = createUser(email = "ub@mail.com", login = "user_b")
        val deckId = createDeck(userBId, "B deck", "desc")
        val cardId = createCard(deckId, "Q", "A")

        val deck = deckRepository.findById(deckId)!!
        deckRepository.update(deck.copy(cardsCount = 1))

        val sessionA = loginUser(login = "user_a")

        mockMvc.perform(delete("/api/v1/decks/$deckId/cards/$cardId").session(sessionA))
            .andExpect(status().isForbidden)

        assertThat(cardRepository.findById(cardId)).isNotNull
    }
}
