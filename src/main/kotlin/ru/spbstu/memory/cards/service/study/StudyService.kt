package ru.spbstu.memory.cards.service.study

import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import ru.spbstu.memory.cards.dto.request.StudyAnswerRequest
import ru.spbstu.memory.cards.dto.request.StudyStatusEnum
import ru.spbstu.memory.cards.dto.response.StudyAnswerResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.persistence.CardRepository
import ru.spbstu.memory.cards.persistence.DeckRepository
import java.time.OffsetDateTime
import java.util.UUID

@Service
class StudyService(
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
) {
    fun processStudyAnswer(
        deckId: UUID,
        userId: UUID,
        req: StudyAnswerRequest,
    ): StudyAnswerResponse =
        transaction {
            val deck =
                deckRepository.findById(deckId)
                    ?: throw NotFoundException(ApiErrorDescription.NOT_FOUND.description)
            if (deck.userId != userId) throw ForbiddenException(ApiErrorDescription.FORBIDDEN.description)

            val card =
                cardRepository.findById(req.cardId)
                    ?: throw NotFoundException(ApiErrorDescription.NOT_FOUND.description)
            if (card.deckId != deckId) throw NotFoundException(ApiErrorDescription.NOT_FOUND.description)

            val updatedCard = card.copy(isLearned = req.status == StudyStatusEnum.REMEMBERED)
            cardRepository.update(updatedCard)

            val learnedCount = cardRepository.getLearnedCount(deckId)
            val learnedPercent = if (deck.cardsCount == 0) 0 else ((learnedCount * 100) / deck.cardsCount).toInt()

            val updatedDeck = deck.copy(learnedPercent = learnedPercent, lastStudied = OffsetDateTime.now())
            deckRepository.update(updatedDeck)

            StudyAnswerResponse(
                learnedPercent = learnedPercent,
            )
        }
}
