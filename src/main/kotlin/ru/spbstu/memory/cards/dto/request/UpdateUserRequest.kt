package ru.spbstu.memory.cards.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateUserRequest(
    @field:Email(message = "invalid email format (RFC 5322)")
    val email: String,
    @field:Size(min = 3, max = 64, message = "login length must be 3..64")
    @field:Pattern(
        regexp = "^[A-Za-z0-9_.-]{3,64}\$",
        message = "login can contain only letters, digits, _ . -",
    )
    val login: String,
)
