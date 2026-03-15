package ru.spbstu.memory.cards.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.spbstu.memory.cards.BaseIntegrationTest

@Tag("integration")
class AuthIntegrationTest : BaseIntegrationTest() {
    @Test
    @DisplayName("1.1 Регистрация, вход и получение списка колод")
    fun register_login_getDecks_shouldCreateUserAuthenticateAndReturnEmptyDeckList() {
        registerUser(
            email = "test@mail.com",
            login = "test_user",
        ).andExpect(status().isCreated)

        val user = userRepository.findByLogin("test_user")
        assertThat(user).isNotNull
        assertThat(user!!.email).isEqualTo("test@mail.com")
        assertThat(passwordEncoder.matches("Password1!", user.passwordHash)).isTrue()

        val session = loginUser(login = "test_user")

        mockMvc.perform(get("/api/v1/decks").session(session))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content").isEmpty)
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
    }

    @Test
    @DisplayName("1.2 Регистрация с дублирующимся email")
    fun register_shouldReturn409_whenEmailAlreadyExists() {
        createUser(email = "exist@mail.com", login = "existing_user")

        registerUser(email = "exist@mail.com", login = "new_user")
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists())
    }
}
