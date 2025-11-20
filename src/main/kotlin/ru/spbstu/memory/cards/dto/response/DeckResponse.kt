package ru.spbstu.memory.cards.dto.response

import ru.spbstu.memory.cards.persistence.model.Deck
import java.time.format.DateTimeFormatter

data class DeckResponse(
    val id: String,
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
                id = deck.id.toString(),
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
