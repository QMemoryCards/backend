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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.spbstu.memory.cards.controller.MeController
import ru.spbstu.memory.cards.dto.request.UpdatePasswordRequest
import ru.spbstu.memory.cards.dto.request.UpdateUserRequest
import ru.spbstu.memory.cards.dto.response.UserResponse
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.user.UserService
import java.time.OffsetDateTime
import java.util.UUID

@WebMvcTest(MeController::class)
class MeControllerMvcTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockitoBean
    private lateinit var userService: UserService

    private val userId = UUID.randomUUID()

    private val userDetails =
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
    fun me_shouldReturnUserInfo_whenAuthenticated() {
        mockMvc.perform(
            get("/api/v1/users/me")
                .with(user(userDetails)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.login").value("testuser"))
            .andExpect(jsonPath("$.createdAt").isNotEmpty)
    }

    @Test
    fun me_shouldReturnUnauthorized_whenNotAuthenticated() {
        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun updateProfile_shouldReturnUpdatedUser_whenAuthenticated() {
        val request =
            UpdateUserRequest(
                email = "new@example.com",
                login = "newuser",
            )
        val response =
            UserResponse(
                id = userId,
                email = "new@example.com",
                login = "newuser",
                createdAt = OffsetDateTime.now().toString(),
            )

        whenever(userService.updateProfile(eq(userId), eq(request))).thenReturn(response)

        mockMvc.perform(
            put("/api/v1/users/me")
                .with(user(userDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.email").value("new@example.com"))
            .andExpect(jsonPath("$.login").value("newuser"))

        verify(userService).updateProfile(userId, request)
    }

    @Test
    fun updatePassword_shouldReturnOk_whenAuthenticated() {
        val request =
            UpdatePasswordRequest(
                currentPassword = "oldPassword123!",
                newPassword = "newPassword123!",
            )

        mockMvc.perform(
            put("/api/v1/users/me/password")
                .with(user(userDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isOk)

        verify(userService).updatePassword(userId, request)
    }

    @Test
    fun deleteAccount_shouldReturnOk_whenAuthenticated() {
        mockMvc.perform(
            delete("/api/v1/users/me")
                .with(user(userDetails))
                .with(csrf()),
        )
            .andExpect(status().isOk)

        verify(userService).deleteUser(userId)
    }
}
