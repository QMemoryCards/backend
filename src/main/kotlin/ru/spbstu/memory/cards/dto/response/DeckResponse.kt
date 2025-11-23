package ru.spbstu.memory.cards.dto.response

import java.util.UUID

data class DeckResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val cardCount: Int,
    val learnedPercent: Int,
    val lastStudied: String?,
    val createdAt: String,
    val updatedAt: String,
)
