package ru.spbstu.memory.cards.persistence.model

import java.time.OffsetDateTime
import java.util.UUID

data class Deck(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val description: String?,
    val learnedPercent: Int,
    val cardsCount: Int,
    val lastStudied: OffsetDateTime?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
