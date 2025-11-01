package ru.spbstu.memory.cards.dto.request

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import ru.spbstu.memory.cards.validation.annotation.ValidPassword

data class LoginRequest(
    @field:Size(min = 3, max = 64)
    @field:Pattern(regexp = "^[A-Za-z0-9_.-]{3,64}\$")
    val login: String,
    @field:ValidPassword
    val password: String,
)
