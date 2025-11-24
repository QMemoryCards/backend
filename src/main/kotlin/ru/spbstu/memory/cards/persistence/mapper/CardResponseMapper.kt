package ru.spbstu.memory.cards.persistence.mapper

import ru.spbstu.memory.cards.dto.response.CardResponse
import ru.spbstu.memory.cards.persistence.model.Card

fun Card.toResponse(): CardResponse =
    CardResponse(
        id = this.id,
        question = this.question,
        answer = this.answer,
        isLearned = this.isLearned,
    )
