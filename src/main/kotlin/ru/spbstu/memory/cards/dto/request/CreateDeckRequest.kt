package ru.spbstu.memory.cards.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateDeckRequest(
    @field:NotBlank
    @field:Size(max = 90)
    val name: String,
    @field:Size(max = 200)
    val description: String?,
)
