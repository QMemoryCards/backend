package ru.spbstu.memory.cards.service.auth

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.spbstu.memory.cards.dto.request.UpdatePasswordRequest
import ru.spbstu.memory.cards.dto.request.UpdateUserRequest
import ru.spbstu.memory.cards.dto.response.UserResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.domain.ConflictException
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.persistence.UserRepository
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun updateProfile(
        userId: UUID,
        req: UpdateUserRequest,
    ): UserResponse {
        if (userRepository.existsByEmailAndIdNot(req.email, userId)) {
            throw ConflictException("email_already_exists")
        }
        if (userRepository.existsByLoginAndIdNot(req.login, userId)) {
            throw ConflictException("login_already_exists")
        }

        val user =
            userRepository.findById(userId)
                ?: throw NotFoundException(ApiErrorCode.NOT_FOUND.code)

        val updatedUser =
            user.copy(
                email = req.email,
                login = req.login,
            )

        val savedUser = userRepository.update(updatedUser)

        return UserResponse(
            id = savedUser.id.toString(),
            email = savedUser.email,
            login = savedUser.login,
            createdAt = savedUser.createdAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        )
    }

    fun updatePassword(
        userId: UUID,
        req: UpdatePasswordRequest,
    ) {
        val user =
            userRepository.findById(userId)
                ?: throw NotFoundException(ApiErrorCode.NOT_FOUND.code)

        if (!passwordEncoder.matches(req.currentPassword, user.passwordHash)) {
            throw ForbiddenException("invalid_current_password")
        }

        val newHash = passwordEncoder.encode(req.newPassword)
        userRepository.updatePassword(userId, newHash)
    }

    fun deleteUser(userId: UUID) {
        if (userRepository.findById(userId) == null) {
            throw NotFoundException(ApiErrorCode.NOT_FOUND.code)
        }
        userRepository.delete(userId)
    }
}
