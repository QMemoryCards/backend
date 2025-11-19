package ru.spbstu.memory.cards.dto.response
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val email: String,
    val login: String,
    val createdAt: String,
)
