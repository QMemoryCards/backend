package ru.spbstu.memory.cards.service.user

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import ru.spbstu.memory.cards.dto.request.UpdatePasswordRequest
import ru.spbstu.memory.cards.dto.request.UpdateUserRequest
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.domain.ConflictException
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.persistence.UserRepository
import ru.spbstu.memory.cards.persistence.model.User
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class UserServiceTest(
    @Mock private val userRepository: UserRepository,
    @Mock private val passwordEncoder: PasswordEncoder,
) {
    private val userService = UserService(userRepository, passwordEncoder)

    private val userId = UUID.randomUUID()
    private val user =
        User(
            id = userId,
            email = "old@example.com",
            login = "olduser",
            passwordHash = "oldHash",
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
        )

    @Test
    fun updateProfile_shouldUpdateUser_whenEmailAndLoginUnique() {
        val request =
            UpdateUserRequest(
                email = "new@example.com",
                login = "newuser",
            )

        whenever(userRepository.existsByEmailAndId(request.email, userId)).thenReturn(true)
        whenever(userRepository.existsByLoginAndId(request.login, userId)).thenReturn(true)
        whenever(userRepository.findById(userId)).thenReturn(user)
        whenever(userRepository.update(any())).thenAnswer { invocation ->
            val updatedUser = invocation.getArgument<User>(0)
            updatedUser.copy(updatedAt = OffsetDateTime.now())
        }

        val result = userService.updateProfile(userId, request)

        assertThat(result.email).isEqualTo(request.email)
        assertThat(result.login).isEqualTo(request.login)
        verify(userRepository).update(any())
    }

    @Test
    fun updateProfile_shouldThrowConflictException_whenEmailTakenByAnotherUser() {
        val request =
            UpdateUserRequest(
                email = "taken@example.com",
                login = "newuser",
            )

        whenever(userRepository.existsByEmailAndId(request.email, userId)).thenReturn(false)

        assertThatThrownBy { userService.updateProfile(userId, request) }
            .isInstanceOf(ConflictException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.EMAIL_CONFLICT)

        verify(userRepository, never()).update(any())
    }

    @Test
    fun updateProfile_shouldThrowConflictException_whenLoginTakenByAnotherUser() {
        val request =
            UpdateUserRequest(
                email = "new@example.com",
                login = "takenuser",
            )

        whenever(userRepository.existsByEmailAndId(request.email, userId)).thenReturn(true)
        whenever(userRepository.existsByLoginAndId(request.login, userId)).thenReturn(false)

        assertThatThrownBy { userService.updateProfile(userId, request) }
            .isInstanceOf(ConflictException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.LOGIN_CONFLICT)

        verify(userRepository, never()).update(any())
    }

    @Test
    fun updatePassword_shouldUpdatePassword_whenCurrentPasswordCorrect() {
        val request =
            UpdatePasswordRequest(
                currentPassword = "currentPassword",
                newPassword = "newPassword123!",
            )

        whenever(userRepository.findById(userId)).thenReturn(user)
        whenever(passwordEncoder.matches(request.currentPassword, user.passwordHash)).thenReturn(true)
        whenever(passwordEncoder.encode(request.newPassword)).thenReturn("newHash")

        userService.updatePassword(userId, request)

        verify(passwordEncoder).matches(request.currentPassword, user.passwordHash)
        verify(passwordEncoder).encode(request.newPassword)
        verify(userRepository).updatePassword(userId, "newHash")
    }

    @Test
    fun updatePassword_shouldThrowForbiddenException_whenCurrentPasswordIncorrect() {
        val request =
            UpdatePasswordRequest(
                currentPassword = "wrongPassword",
                newPassword = "newPassword123!",
            )

        whenever(userRepository.findById(userId)).thenReturn(user)
        whenever(passwordEncoder.matches(request.currentPassword, user.passwordHash)).thenReturn(false)

        assertThatThrownBy { userService.updatePassword(userId, request) }
            .isInstanceOf(ForbiddenException::class.java)

        verify(passwordEncoder).matches(request.currentPassword, user.passwordHash)
        verify(userRepository, never()).updatePassword(any(), any())
    }

    @Test
    fun deleteUser_shouldDeleteUser_whenUserExists() {
        whenever(userRepository.findById(userId)).thenReturn(user)

        userService.deleteUser(userId)

        verify(userRepository).findById(userId)
        verify(userRepository).delete(userId)
    }

    @Test
    fun deleteUser_shouldThrowNotFoundException_whenUserNotExists() {
        whenever(userRepository.findById(userId)).thenReturn(null)

        assertThatThrownBy { userService.deleteUser(userId) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.USER_NOT_FOUND)

        verify(userRepository, never()).delete(any())
    }
}
