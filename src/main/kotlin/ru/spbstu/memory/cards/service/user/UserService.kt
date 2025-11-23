package ru.spbstu.memory.cards.service.user

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.spbstu.memory.cards.dto.request.UpdatePasswordRequest
import ru.spbstu.memory.cards.dto.request.UpdateUserRequest
import ru.spbstu.memory.cards.dto.response.UserResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription
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
        request: UpdateUserRequest,
    ): UserResponse {
        if (!userRepository.existsByEmailAndId(request.email, userId)) {
            throw ConflictException(code = ApiErrorCode.EMAIL_CONFLICT)
        }
        if (!userRepository.existsByLoginAndId(request.login, userId)) {
            throw ConflictException(code = ApiErrorCode.LOGIN_CONFLICT)
        }

        val user =
            userRepository.findById(userId)
                ?: throw NotFoundException(code = ApiErrorCode.USER_NOT_FOUND)

        val updatedUser =
            user.copy(
                email = request.email,
                login = request.login,
            )

        val savedUser = userRepository.update(updatedUser)

        return UserResponse(
            id = savedUser.id,
            email = savedUser.email,
            login = savedUser.login,
            createdAt = savedUser.createdAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        )
    }

    fun updatePassword(
        userId: UUID,
        request: UpdatePasswordRequest,
    ) {
        val user =
            userRepository.findById(userId)
                ?: throw NotFoundException(code = ApiErrorCode.USER_NOT_FOUND)

        if (!passwordEncoder.matches(request.currentPassword, user.passwordHash)) {
            throw ForbiddenException(ApiErrorDescription.FORBIDDEN.description)
        }

        val newHash = passwordEncoder.encode(request.newPassword)
        userRepository.updatePassword(userId, newHash)
    }

    fun deleteUser(userId: UUID) {
        if (userRepository.findById(userId) == null) {
            throw NotFoundException(code = ApiErrorCode.USER_NOT_FOUND)
        }
        userRepository.delete(userId)
    }
}
