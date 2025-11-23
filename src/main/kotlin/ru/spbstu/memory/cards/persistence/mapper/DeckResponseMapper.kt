package ru.spbstu.memory.cards.persistence.mapper

import ru.spbstu.memory.cards.dto.response.DeckResponse
import ru.spbstu.memory.cards.persistence.model.Deck
import java.time.format.DateTimeFormatter

fun Deck.toResponse(): DeckResponse =
    DeckResponse(
        id = this.id,
        name = this.name,
        description = this.description,
        cardCount = this.cardsCount,
        learnedPercent = this.learnedPercent,
        lastStudied = this.lastStudied?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        createdAt = this.createdAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        updatedAt = this.updatedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    )
