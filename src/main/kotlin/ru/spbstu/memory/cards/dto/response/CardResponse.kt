package ru.spbstu.memory.cards.dto.response

import java.util.UUID

data class CardResponse(
    val id: UUID,
    val question: String,
    val answer: String,
    val isLearned: Boolean,
)
