package ru.spbstu.memory.cards.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.spbstu.memory.cards.dto.request.UpdatePasswordRequest
import ru.spbstu.memory.cards.dto.request.UpdateUserRequest
import ru.spbstu.memory.cards.dto.response.UserResponse
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.user.UserService
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class MeControllerTest(
    @Mock private val userService: UserService,
) {
    private val controller = MeController(userService)

    private val userId = UUID.randomUUID()

    private val principal =
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
        val result = controller.me(principal)

        assertThat(result.id).isEqualTo(userId)
        assertThat(result.email).isEqualTo("test@example.com")
        assertThat(result.login).isEqualTo("testuser")
        assertThat(result.createdAt).isNotBlank
    }

    @Test
    fun me_shouldThrowUnauthorized_whenNotAuthenticated() {
        assertThatThrownBy { controller.me(null) }
            .isInstanceOf(UnauthorizedException::class.java)
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

        val result = controller.updateProfile(principal, request)

        assertThat(result.email).isEqualTo("new@example.com")
        assertThat(result.login).isEqualTo("newuser")
        verify(userService).updateProfile(userId, request)
    }

    @Test
    fun updatePassword_shouldReturnOk_whenAuthenticated() {
        val request =
            UpdatePasswordRequest(
                currentPassword = "oldPassword123!",
                newPassword = "newPassword123!",
            )

        controller.updatePassword(principal, request)

        verify(userService).updatePassword(userId, request)
    }

    @Test
    fun deleteAccount_shouldReturnOk_whenAuthenticated() {
        val session = mock<HttpSession>()
        val httpRequest = mock<HttpServletRequest>()
        whenever(httpRequest.getSession(false)).thenReturn(session)

        controller.deleteAccount(principal, httpRequest)

        verify(userService).deleteUser(userId)
        verify(session).invalidate()
    }

    @Test
    fun deleteAccount_shouldNotInvalidateSession_whenNoSession() {
        val httpRequest = mock<HttpServletRequest>()
        whenever(httpRequest.getSession(false)).thenReturn(null)

        controller.deleteAccount(principal, httpRequest)

        verify(userService).deleteUser(userId)
        verify(httpRequest).getSession(false)
    }

    @Test
    fun updateProfile_shouldThrowUnauthorized_whenPrincipalNull() {
        val request = UpdateUserRequest(email = "new@example.com", login = "newuser")

        assertThatThrownBy { controller.updateProfile(null, request) }
            .isInstanceOf(UnauthorizedException::class.java)

        verify(userService, never()).updateProfile(eq(userId), eq(request))
    }

    @Test
    fun deleteAccount_shouldInvalidateSession_whenSessionExists() {
        val request = mock<HttpServletRequest>()
        val session = mock<HttpSession>()

        whenever(request.getSession(false)).thenReturn(session)

        controller.deleteAccount(principal, request)

        verify(session).invalidate()
    }

    @Test
    fun deleteAccount_shouldNotFail_whenSessionMissing() {
        val request = mock<HttpServletRequest>()
        whenever(request.getSession(false)).thenReturn(null)

        assertDoesNotThrow { controller.deleteAccount(principal, request) }
    }
}
