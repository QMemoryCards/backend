package ru.spbstu.memory.cards.dto.request

import jakarta.validation.constraints.Size

data class ImportSharedDeckRequest(
    @field:Size(min = 1, max = 90)
    val newName: String,
    @field:Size(max = 200)
    val newDescription: String?,
)
