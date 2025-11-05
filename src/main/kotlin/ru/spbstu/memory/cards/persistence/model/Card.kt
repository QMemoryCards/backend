package ru.spbstu.memory.cards.persistence.model

import java.time.OffsetDateTime
import java.util.UUID

data class Card(
    val id: UUID,
    val deckId: UUID,
    val question: String,
    val answer: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
