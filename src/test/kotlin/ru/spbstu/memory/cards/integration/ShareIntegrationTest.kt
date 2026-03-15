package ru.spbstu.memory.cards.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.spbstu.memory.cards.BaseIntegrationTest
import ru.spbstu.memory.cards.dto.request.ImportSharedDeckRequest
import java.util.UUID

class ShareIntegrationTest : BaseIntegrationTest() {
    @Test
    @DisplayName("9.1 Создание токена и успешный импорт колоды")
    fun importSharedDeck_shouldCopyDeckAndCards() {
        val ownerId = createUser(email = "owner_share@mail.com", login = "share_owner")
        val importerId = createUser(email = "importer@mail.com", login = "importer")

        val sourceDeckId = createDeck(ownerId, "Shared Deck", "desc")
        createCard(sourceDeckId, "Q1", "A1")
        createCard(sourceDeckId, "Q2", "A2")

        val sourceDeck = deckRepository.findById(sourceDeckId)!!
        deckRepository.update(sourceDeck.copy(cardsCount = 2))

        val ownerSession = loginUser(login = "share_owner")
        val importerSession = loginUser(login = "importer")

        val shareResult =
            mockMvc.perform(post("/api/v1/decks/$sourceDeckId/share").session(ownerSession))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.url").exists())
                .andReturn()

        val token = objectMapper.readTree(shareResult.response.contentAsString).get("token").asText()

        mockMvc.perform(
            post("/api/v1/share/$token/import")
                .session(importerSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        ImportSharedDeckRequest(
                            newName = "Imported deck",
                            newDescription = "copied deck",
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Imported deck"))
            .andExpect(jsonPath("$.description").value("copied deck"))
            .andExpect(jsonPath("$.cardCount").value(2))

        val importedDecks = deckRepository.findAllByUserId(importerId, 0, 20)
        assertThat(importedDecks.items).hasSize(1)

        val importedDeck = importedDecks.items.first()
        assertThat(importedDeck.id).isNotEqualTo(sourceDeckId)
        assertThat(importedDeck.name).isEqualTo("Imported deck")
        assertThat(importedDeck.cardsCount).isEqualTo(2)

        val importedCards = cardRepository.findAllByDeckId(importedDeck.id)
        assertThat(importedCards).hasSize(2)
        assertThat(importedCards.map { it.question }).containsExactlyInAnyOrder("Q1", "Q2")
    }

    @Test
    @DisplayName("9.2 Импорт колоды по несуществующему токену")
    fun importSharedDeck_shouldReturn404_whenTokenDoesNotExist() {
        createUser(email = "owner_share@mail.com", login = "share_owner")
        val session = loginUser(login = "share_owner")

        mockMvc.perform(
            post("/api/v1/share/${UUID.randomUUID()}/import")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        ImportSharedDeckRequest(
                            newName = "Imported deck",
                            newDescription = "copied deck",
                        ),
                    ),
                ),
        )
            .andExpect(status().isNotFound)
    }
}
