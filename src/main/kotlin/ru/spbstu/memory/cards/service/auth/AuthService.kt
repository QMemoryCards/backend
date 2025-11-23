package ru.spbstu.memory.cards.service.auth

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.spbstu.memory.cards.dto.request.RegisterRequest
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.domain.ConflictException
import ru.spbstu.memory.cards.persistence.UserRepository

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun register(req: RegisterRequest) {
        if (userRepository.existsByEmail(req.email)) {
            throw ConflictException(code = ApiErrorCode.EMAIL_CONFLICT)
        }
        if (userRepository.existsByLogin(req.login)) {
            throw ConflictException(code = ApiErrorCode.LOGIN_CONFLICT)
        }

        userRepository.saveNew(
            email = req.email,
            login = req.login,
            passwordHash = passwordEncoder.encode(req.password),
        )
    }
}
