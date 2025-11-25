package ru.spbstu.memory.cards.dto.response

data class DeckShareResponse(
    val name: String,
    val description: String?,
    val cardCount: Int,
)
