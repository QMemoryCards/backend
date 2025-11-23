package ru.spbstu.memory.cards.service.card

import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import ru.spbstu.memory.cards.dto.request.CreateCardRequest
import ru.spbstu.memory.cards.dto.response.CardResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.LimitExceededException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.persistence.CardRepository
import ru.spbstu.memory.cards.persistence.DeckRepository
import ru.spbstu.memory.cards.persistence.mapper.toResponse
import ru.spbstu.memory.cards.persistence.model.PaginatedResult
import java.util.UUID

@Service
class CardService(
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
) {
    fun getCards(
        deckId: UUID,
        userId: UUID,
        page: Int,
        size: Int,
    ): PaginatedResult<CardResponse> {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(ApiErrorCode.NOT_FOUND.code)
        if (deck.userId != userId) throw ForbiddenException(ApiErrorCode.FORBIDDEN.code)

        val repoResult = cardRepository.findAllByDeckIdPaginated(deckId, page, size)

        return PaginatedResult(
            items = repoResult.items.map { it.toResponse() },
            total = repoResult.total,
        )
    }

    fun createCard(
        deckId: UUID,
        userId: UUID,
        req: CreateCardRequest,
    ): CardResponse =
        transaction {
            val deck =
                deckRepository.findById(deckId)
                    ?: throw NotFoundException(ApiErrorCode.NOT_FOUND.code)
            if (deck.userId != userId) throw ForbiddenException(ApiErrorCode.FORBIDDEN.code)

            if (deck.cardsCount >= 30) {
                throw LimitExceededException(ApiErrorCode.LIMIT_EXCEEDED.code)
            }

            val card = cardRepository.saveNew(deckId, req.question, req.answer)

            val updatedDeck = deck.copy(cardsCount = deck.cardsCount + 1)
            deckRepository.update(updatedDeck)

            card.toResponse()
        }

    fun updateCard(
        deckId: UUID,
        cardId: UUID,
        userId: UUID,
        req: CreateCardRequest,
    ): CardResponse {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(ApiErrorCode.NOT_FOUND.code)
        if (deck.userId != userId) throw ForbiddenException(ApiErrorCode.FORBIDDEN.code)

        val card =
            cardRepository.findById(cardId)
                ?: throw NotFoundException(ApiErrorCode.NOT_FOUND.code)
        if (card.deckId != deckId) throw NotFoundException(ApiErrorCode.NOT_FOUND.code)

        val updatedCard = card.copy(question = req.question, answer = req.answer)
        return cardRepository.update(updatedCard).toResponse()
    }

    fun deleteCard(
        deckId: UUID,
        cardId: UUID,
        userId: UUID,
    ) = transaction {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(ApiErrorCode.NOT_FOUND.code)
        if (deck.userId != userId) throw ForbiddenException(ApiErrorCode.FORBIDDEN.code)

        val card =
            cardRepository.findById(cardId)
                ?: throw NotFoundException(ApiErrorCode.NOT_FOUND.code)
        if (card.deckId != deckId) throw NotFoundException(ApiErrorCode.NOT_FOUND.code)

        cardRepository.delete(cardId)

        val newCount = if (deck.cardsCount > 0) deck.cardsCount - 1 else 0
        deckRepository.update(deck.copy(cardsCount = newCount))
    }
}
