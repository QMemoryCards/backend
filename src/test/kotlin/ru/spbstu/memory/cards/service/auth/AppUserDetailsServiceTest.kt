package ru.spbstu.memory.cards.service.auth

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UsernameNotFoundException
import ru.spbstu.memory.cards.persistence.UserRepository
import ru.spbstu.memory.cards.persistence.model.User
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AppUserDetailsServiceTest(
    @Mock private val userRepository: UserRepository,
) {
    private val appUserDetailsService = AppUserDetailsService(userRepository)

    @Test
    fun loadUserByUsername_shouldReturnAppUserDetails_whenUserExists() {
        val user =
            User(
                id = UUID.randomUUID(),
                email = "test@example.com",
                login = "testuser",
                passwordHash = "hashedPassword",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        whenever(userRepository.findByLogin("testuser")).thenReturn(user)

        val result = appUserDetailsService.loadUserByUsername("testuser")

        assertThat(result).isNotNull
        assertThat(result.username).isEqualTo("testuser")
        assertThat(result.password).isEqualTo("hashedPassword")
        assertThat(result.isEnabled).isTrue
    }

    @Test
    fun loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserNotExists() {
        whenever(userRepository.findByLogin("nonexistent")).thenReturn(null)

        assertThatThrownBy { appUserDetailsService.loadUserByUsername("nonexistent") }
            .isInstanceOf(UsernameNotFoundException::class.java)
    }
}
