package ru.spbstu.memory.cards.service.deck

import org.springframework.stereotype.Service
import ru.spbstu.memory.cards.dto.request.CreateDeckRequest
import ru.spbstu.memory.cards.dto.response.DeckResponse
import ru.spbstu.memory.cards.dto.response.PageResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription
import ru.spbstu.memory.cards.exception.domain.ConflictException
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.LimitExceededException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.persistence.DeckRepository
import java.util.UUID
import kotlin.math.ceil

@Service
class DeckService(
    private val deckRepository: DeckRepository,
) {
    fun createDeck(
        userId: UUID,
        request: CreateDeckRequest,
    ): DeckResponse {
        val count = deckRepository.countByUserId(userId)
        if (count >= 30) {
            throw LimitExceededException(ApiErrorDescription.LIMIT_EXCEEDED.description)
        }

        if (deckRepository.existsByUserIdAndName(userId, request.name)) {
            throw ConflictException(ApiErrorDescription.CONFLICT.description)
        }

        val deck = deckRepository.saveNew(userId, request.name, request.description)
        return DeckResponse.from(deck)
    }

    fun getDecks(
        userId: UUID,
        page: Int,
        size: Int,
    ): PageResponse<DeckResponse> {
        val (decks, total) = deckRepository.findAllByUserId(userId, page, size)

        val totalPages = if (size > 0) ceil(total.toDouble() / size).toInt() else 0

        return PageResponse(
            content = decks.map { DeckResponse.from(it) },
            totalElements = total,
            totalPages = totalPages,
            page = page,
            size = size,
        )
    }

    fun getDeck(
        deckId: UUID,
        userId: UUID,
    ): DeckResponse {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(ApiErrorDescription.NOT_FOUND.description)

        if (deck.userId != userId) throw ForbiddenException(ApiErrorDescription.FORBIDDEN.description)

        return DeckResponse.from(deck)
    }

    fun updateDeck(
        deckId: UUID,
        userId: UUID,
        request: CreateDeckRequest,
    ): DeckResponse {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(ApiErrorDescription.NOT_FOUND.description)

        if (deck.userId != userId) throw ForbiddenException(ApiErrorDescription.FORBIDDEN.description)

        if (!deckRepository.existsByUserIdAndNameAndId(userId, request.name, deckId)) {
            throw ConflictException(ApiErrorDescription.CONFLICT.description)
        }

        val updatedDeck =
            deck.copy(
                name = request.name,
                description = request.description,
            )
        return DeckResponse.from(deckRepository.update(updatedDeck))
    }

    fun deleteDeck(
        deckId: UUID,
        userId: UUID,
    ) {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(ApiErrorDescription.NOT_FOUND.description)

        if (deck.userId != userId) throw ForbiddenException(ApiErrorDescription.FORBIDDEN.description)

        deckRepository.delete(deckId)
    }
}
