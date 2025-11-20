package ru.spbstu.memory.cards.dto.response

import ru.spbstu.memory.cards.persistence.model.Card

data class CardResponse(
    val id: String,
    val question: String,
    val answer: String,
) {
    companion object {
        fun from(card: Card) =
            CardResponse(
                id = card.id.toString(),
                question = card.question,
                answer = card.answer,
            )
    }
}
