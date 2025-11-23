package ru.spbstu.memory.cards.dto.response

import ru.spbstu.memory.cards.persistence.model.Deck
import java.time.format.DateTimeFormatter
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
) {
    companion object {
        fun from(deck: Deck): DeckResponse =
            DeckResponse(
                id = deck.id,
                name = deck.name,
                description = deck.description,
                cardCount = deck.cardsCount,
                learnedPercent = deck.learnedPercent,
                lastStudied = deck.lastStudied?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                createdAt = deck.createdAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                updatedAt = deck.updatedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            )
    }
}
