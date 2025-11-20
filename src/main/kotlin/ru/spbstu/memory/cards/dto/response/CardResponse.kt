package ru.spbstu.memory.cards.dto.response

import ru.spbstu.memory.cards.persistence.model.Card
import java.util.UUID

data class CardResponse(
    val id: UUID,
    val question: String,
    val answer: String,
) {
    companion object {
        fun from(card: Card) =
            CardResponse(
                id = card.id,
                question = card.question,
                answer = card.answer,
            )
    }
}
