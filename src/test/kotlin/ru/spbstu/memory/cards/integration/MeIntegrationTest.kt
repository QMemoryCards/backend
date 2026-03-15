package ru.spbstu.memory.cards.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.spbstu.memory.cards.BaseIntegrationTest
import ru.spbstu.memory.cards.dto.request.UpdateUserRequest

class MeIntegrationTest : BaseIntegrationTest() {
    @Test
    @DisplayName("2.1 Удаление авторизованного пользователя")
    fun deleteAccount_shouldDeleteAuthenticatedUser() {
        registerUser(
            email = "delete_me@mail.com",
            login = "user_to_delete",
        ).andExpect(status().isCreated)

        val session = loginUser(login = "user_to_delete")
        val userId = userRepository.findByLogin(login = "user_to_delete")!!.id
        val deckId = createDeck(userId = userId, name = "My Deck", description = "to delete")

        mockMvc.perform(delete("/api/v1/users/me").session(session))
            .andExpect(status().isOk)

        assertThat(userRepository.findById(userId)).isNull()
        assertThat(deckRepository.findById(deckId)).isNull()
    }

    @Test
    @DisplayName("2.2 Удаление аккаунта без авторизации")
    fun deleteAccount_shouldReturn403_whenUnauthorized() {
        val userId =
            createUser(
                email = "alive@mail.com",
                login = "alive_user",
            )
        val deckId = createDeck(userId, "Alive Deck", "still exists")

        mockMvc.perform(delete("/api/v1/users/me"))
            .andExpect(status().isUnauthorized)

        assertThat(userRepository.findById(userId)).isNotNull
        assertThat(deckRepository.findById(deckId)).isNotNull
    }

    @Test
    @DisplayName("3.1 Успешное обновление email и логина")
    fun updateProfile_shouldUpdateEmailAndLogin() {
        createUser(
            email = "old@mail.com",
            login = "old_login",
        )
        val session = loginUser(login = "old_login")

        mockMvc.perform(
            put("/api/v1/users/me")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        UpdateUserRequest(
                            email = "new_email@mail.com",
                            login = "new_login",
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("new_email@mail.com"))
            .andExpect(jsonPath("$.login").value("new_login"))

        val updated = userRepository.findByLogin("new_login")
        assertThat(updated).isNotNull
        assertThat(updated!!.email).isEqualTo("new_email@mail.com")
    }

    @Test
    @DisplayName("3.2 Обновление логина с недопустимыми символами")
    fun updateProfile_shouldReturn400_whenLoginIsInvalid() {
        val id =
            createUser(
                email = "valid@mail.com",
                login = "valid_login",
            )
        val session = loginUser(login = "valid_login")

        mockMvc.perform(
            put("/api/v1/users/me")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        UpdateUserRequest(
                            email = "valid2@mail.com",
                            login = "русский_логин",
                        ),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)

        val notUpdated = userRepository.findById(id)
        assertThat(notUpdated).isNotNull
        assertThat(notUpdated!!.login).isEqualTo("valid_login")
    }
}
