package ru.spbstu.memory.cards.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import ru.spbstu.memory.cards.dto.request.LoginRequest
import ru.spbstu.memory.cards.dto.request.RegisterRequest
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.service.auth.AuthService

@ExtendWith(MockitoExtension::class)
class AuthControllerTest(
    @Mock private val authService: AuthService,
    @Mock private val authenticationManager: AuthenticationManager,
    @Mock private val request: HttpServletRequest,
    @Mock private val session: HttpSession,
    @Mock private val authentication: Authentication,
) {
    private val controller = AuthController(authService, authenticationManager)

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun register_shouldCallAuthServiceRegister() {
        val body =
            RegisterRequest(
                login = "testuser",
                email = "test@example.com",
                password = "password",
            )

        controller.register(body)

        verify(authService).register(eq(body))
    }

    @Test
    fun login_shouldAuthenticateAndStoreSecurityContextInSession() {
        val body =
            LoginRequest(
                login = "testuser",
                password = "password",
            )

        whenever(request.getSession(true)).thenReturn(session)

        whenever(authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>()))
            .thenReturn(authentication)

        controller.login(body, request)

        verify(authenticationManager).authenticate(
            eq(UsernamePasswordAuthenticationToken(body.login, body.password)),
        )

        val context = SecurityContextHolder.getContext()
        Assertions.assertThat(context.authentication).isSameAs(authentication)

        verify(session).setAttribute(
            eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY),
            eq(context),
        )
    }

    @Test
    fun logout_shouldInvalidateSession_whenSessionExists() {
        whenever(request.getSession(false)).thenReturn(session)

        controller.logout(request)

        verify(session).invalidate()
    }

    @Test
    fun logout_shouldThrowUnauthorized_whenSessionIsMissing() {
        whenever(request.getSession(false)).thenReturn(null)

        Assertions.assertThatThrownBy { controller.logout(request) }
            .isInstanceOf(UnauthorizedException::class.java)
    }
}
