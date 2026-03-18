package ru.spbstu.memory.cards.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.spbstu.memory.cards.BaseIntegrationTest
import ru.spbstu.memory.cards.dto.request.CreateDeckRequest

class DeckIntegrationTest : BaseIntegrationTest() {
    @Test
    @DisplayName("4.1 Создание новой учебной колоды")
    fun createDeck_shouldCreateDeckAndReturnIt() {
        createUser(email = "deck_user@mail.com", login = "deck_user")
        val session = loginUser(login = "deck_user")
        val userId = userRepository.findByLogin("deck_user")!!.id

        mockMvc.perform(
            post("/api/v1/decks")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        CreateDeckRequest(
                            name = "English Verbs",
                            description = "Irregular verbs for exam",
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("English Verbs"))
            .andExpect(jsonPath("$.description").value("Irregular verbs for exam"))
            .andExpect(jsonPath("$.cardCount").value(0))
            .andExpect(jsonPath("$.learnedPercent").value(0))

        val decks = deckRepository.findAllByUserId(userId, 0, 20)
        assertThat(decks.items).hasSize(1)
        assertThat(decks.items.first().name).isEqualTo("English Verbs")
    }

    @Test
    @DisplayName("5.1 Удаление собственной колоды")
    fun deleteDeck_shouldDeleteOwnDeck() {
        val userId = createUser(email = "deck_user@mail.com", login = "deck_user")
        val deckId = createDeck(userId = userId, name = "Delete me", description = "desc")
        val session = loginUser(login = "deck_user")

        mockMvc.perform(delete("/api/v1/decks/$deckId").session(session))
            .andExpect(status().isOk)

        mockMvc.perform(get("/api/v1/decks/$deckId").session(session))
            .andExpect(status().isNotFound)

        assertThat(deckRepository.findById(deckId)).isNull()
    }

    @Test
    @DisplayName("5.2 Удаление чужой колоды")
    fun deleteDeck_shouldReturn403_whenDeletingForeignDeck() {
        createUser("userA@mail.com", "userA", "Password1!")
        val userBId = createUser("userB@mail.com", "userB", "Password1!")
        val deckId = createDeck(userBId, "B deck", "desc")

        val session = loginUser("userA", "Password1!")

        mockMvc.perform(delete("/api/v1/decks/$deckId").session(session))
            .andExpect(status().isForbidden)

        assertThat(deckRepository.findById(deckId)).isNotNull
    }

    @Test
    @DisplayName("8.1 Получение детальной информации о колоде")
    fun getDeck_shouldReturnDeckDetails() {
        val userId = createUser(email = "deck_user@mail.com", login = "deck_user")
        val deckId = createDeck(userId = userId, "Deck", "Description")
        val session = loginUser(login = "deck_user")

        mockMvc.perform(get("/api/v1/decks/$deckId").session(session))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(deckId.toString()))
            .andExpect(jsonPath("$.name").value("Deck"))
            .andExpect(jsonPath("$.description").value("Description"))
            .andExpect(jsonPath("$.cardCount").value(0))
            .andExpect(jsonPath("$.learnedPercent").value(0))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists())
    }
}
