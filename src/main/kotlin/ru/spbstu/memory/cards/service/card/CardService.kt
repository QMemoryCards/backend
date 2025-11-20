package ru.spbstu.memory.cards.service.card

import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import ru.spbstu.memory.cards.dto.request.CreateCardRequest
import ru.spbstu.memory.cards.dto.response.CardResponse
import ru.spbstu.memory.cards.dto.response.PageResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.LimitExceededException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.persistence.CardRepository
import ru.spbstu.memory.cards.persistence.DeckRepository
import java.util.UUID
import kotlin.math.ceil

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
    ): PageResponse<CardResponse> {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(ApiErrorDescription.NOT_FOUND.description)
        if (deck.userId != userId) throw ForbiddenException("Access denied")

        val (cards, total) = cardRepository.findAllByDeckIdPaginated(deckId, page, size)
        val totalPages = if (size > 0) ceil(total.toDouble() / size).toInt() else 0

        return PageResponse(
            content = cards.map { CardResponse.from(it) },
            totalElements = total,
            totalPages = totalPages,
            page = page,
            size = size,
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
                    ?: throw NotFoundException(ApiErrorDescription.NOT_FOUND.description)
            if (deck.userId != userId) throw ForbiddenException("Access denied")
            if (deck.cardsCount >= 30) {
                throw LimitExceededException("Deck cannot have more than 30 cards")
            }
            val card = cardRepository.saveNew(deckId, req.question, req.answer)
            val updatedDeck = deck.copy(cardsCount = deck.cardsCount + 1)
            deckRepository.update(updatedDeck)

            CardResponse.from(card)
        }

    fun updateCard(
        deckId: UUID,
        cardId: UUID,
        userId: UUID,
        req: CreateCardRequest,
    ): CardResponse {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(ApiErrorDescription.NOT_FOUND.description)
        if (deck.userId != userId) throw ForbiddenException("Access denied")

        val card =
            cardRepository.findById(cardId)
                ?: throw NotFoundException("Card not found")

        if (card.deckId != deckId) throw NotFoundException("Card not found in this deck")

        val updatedCard = card.copy(question = req.question, answer = req.answer)
        return CardResponse.from(cardRepository.update(updatedCard))
    }

    fun deleteCard(
        deckId: UUID,
        cardId: UUID,
        userId: UUID,
    ) = transaction {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(ApiErrorDescription.NOT_FOUND.description)
        if (deck.userId != userId) throw ForbiddenException("Access denied")

        val card = cardRepository.findById(cardId) ?: throw NotFoundException("Card not found")
        if (card.deckId != deckId) throw NotFoundException("Card not found in this deck")

        cardRepository.delete(cardId)

        val newCount = if (deck.cardsCount > 0) deck.cardsCount - 1 else 0
        deckRepository.update(deck.copy(cardsCount = newCount))
    }
}
