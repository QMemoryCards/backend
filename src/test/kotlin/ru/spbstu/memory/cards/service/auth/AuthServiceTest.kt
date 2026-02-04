package ru.spbstu.memory.cards.service.auth

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
import ru.spbstu.memory.cards.dto.request.RegisterRequest
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.domain.ConflictException
import ru.spbstu.memory.cards.persistence.UserRepository

@ExtendWith(MockitoExtension::class)
class AuthServiceTest(
    @Mock private val userRepository: UserRepository,
    @Mock private val passwordEncoder: PasswordEncoder,
) {
    private val authService = AuthService(userRepository, passwordEncoder)

    private val registerRequest =
        RegisterRequest(
            email = "test@example.com",
            login = "testuser",
            password = "Password123!",
        )

    @Test
    fun register_shouldSaveUser_whenEmailAndLoginUnique() {
        whenever(userRepository.existsByEmail(registerRequest.email)).thenReturn(false)
        whenever(userRepository.existsByLogin(registerRequest.login)).thenReturn(false)
        whenever(passwordEncoder.encode(registerRequest.password)).thenReturn("encodedPassword")

        authService.register(registerRequest)

        verify(userRepository).existsByEmail(registerRequest.email)
        verify(userRepository).existsByLogin(registerRequest.login)
        verify(passwordEncoder).encode(registerRequest.password)
        verify(userRepository).saveNew(
            email = registerRequest.email,
            login = registerRequest.login,
            passwordHash = "encodedPassword",
        )
    }

    @Test
    fun register_shouldThrowConflictException_whenEmailExists() {
        whenever(userRepository.existsByEmail(registerRequest.email)).thenReturn(true)

        assertThatThrownBy { authService.register(registerRequest) }
            .isInstanceOf(ConflictException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.EMAIL_CONFLICT)

        verify(userRepository).existsByEmail(registerRequest.email)
        verify(userRepository, never()).saveNew(any(), any(), any())
    }

    @Test
    fun register_shouldThrowConflictException_whenLoginExists() {
        whenever(userRepository.existsByEmail(registerRequest.email)).thenReturn(false)
        whenever(userRepository.existsByLogin(registerRequest.login)).thenReturn(true)

        assertThatThrownBy { authService.register(registerRequest) }
            .isInstanceOf(ConflictException::class.java)
            .hasFieldOrPropertyWithValue("code", ApiErrorCode.LOGIN_CONFLICT)

        verify(userRepository).existsByEmail(registerRequest.email)
        verify(userRepository).existsByLogin(registerRequest.login)
        verify(userRepository, never()).saveNew(any(), any(), any())
    }
}
