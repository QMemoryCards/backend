package ru.spbstu.memory.cards.dto.request

import ru.spbstu.memory.cards.validation.annotation.ValidPassword

data class UpdatePasswordRequest(
    val currentPassword: String,
    @field:ValidPassword
    val newPassword: String,
)
