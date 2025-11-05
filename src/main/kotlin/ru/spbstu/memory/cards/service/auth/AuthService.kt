package ru.spbstu.memory.cards.service.auth

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.spbstu.memory.cards.dto.request.RegisterRequest

@Service
class AuthService(
    private val passwordEncoder: PasswordEncoder,
) {
    fun register(req: RegisterRequest) {
        TODO()
    }
}
