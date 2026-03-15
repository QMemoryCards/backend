package ru.spbstu.memory.cards

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import ru.spbstu.memory.cards.dto.request.LoginRequest
import ru.spbstu.memory.cards.dto.request.RegisterRequest
import ru.spbstu.memory.cards.persistence.CardRepository
import ru.spbstu.memory.cards.persistence.DeckRepository
import ru.spbstu.memory.cards.persistence.DeckSharesRepository
import ru.spbstu.memory.cards.persistence.UserRepository
import ru.spbstu.memory.cards.persistence.config.BasePostgresTest
import java.util.UUID

@AutoConfigureMockMvc
abstract class BaseIntegrationTest : BasePostgresTest() {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    protected lateinit var cardRepository: CardRepository

    @Autowired
    protected lateinit var deckRepository: DeckRepository

    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var shareRepository: DeckSharesRepository

    protected fun registerUser(
        email: String,
        login: String,
        password: String = "Password1!",
    ): ResultActions =
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        RegisterRequest(
                            email = email,
                            login = login,
                            password = password,
                        ),
                    ),
                ),
        )

    protected fun loginUser(
        login: String,
        password: String = "Password1!",
    ): MockHttpSession {
        val result =
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            LoginRequest(
                                login = login,
                                password = password,
                            ),
                        ),
                    ),
            )
                .andReturn()

        Assertions.assertThat(result.response.status).isEqualTo(200)
        return result.request.session as MockHttpSession
    }

    protected fun createUser(
        email: String,
        login: String,
        rawPassword: String = "Password1!",
    ): UUID =
        userRepository.saveNew(
            email = email,
            login = login,
            passwordHash = passwordEncoder.encode(rawPassword),
        ).id

    protected fun createDeck(
        userId: UUID,
        name: String = "Deck",
        description: String? = "Description",
    ): UUID = deckRepository.saveNew(userId, name, description).id

    protected fun createCard(
        deckId: UUID,
        question: String = "Question",
        answer: String = "Answer",
    ): UUID = cardRepository.saveNew(deckId, question, answer).id
}
