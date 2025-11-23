package ru.spbstu.memory.cards.service.deck

import org.springframework.stereotype.Service
import ru.spbstu.memory.cards.dto.request.CreateDeckRequest
import ru.spbstu.memory.cards.dto.response.DeckResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.domain.ConflictException
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.LimitExceededException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.persistence.DeckRepository
import ru.spbstu.memory.cards.persistence.mapper.toResponse
import ru.spbstu.memory.cards.persistence.model.PaginatedResult
import java.util.UUID

@Service
class DeckService(
    private val deckRepository: DeckRepository,
) {
    fun createDeck(
        userId: UUID,
        req: CreateDeckRequest,
    ): DeckResponse {
        val count = deckRepository.countByUserId(userId)
        if (count >= 30) {
            throw LimitExceededException(ApiErrorCode.LIMIT_EXCEEDED.code)
        }

        if (deckRepository.existsByUserIdAndName(userId, req.name)) {
            throw ConflictException(ApiErrorCode.CONFLICT.code)
        }

        val deck = deckRepository.saveNew(userId, req.name, req.description)
        return deck.toResponse()
    }

    fun getDecks(
        userId: UUID,
        page: Int,
        size: Int,
    ): PaginatedResult<DeckResponse> {
        val repoResult = deckRepository.findAllByUserId(userId, page, size)
        val dtos = repoResult.items.map { it.toResponse() }

        return PaginatedResult(
            items = dtos,
            total = repoResult.total,
        )
    }

    fun getDeck(
        deckId: UUID,
        userId: UUID,
    ): DeckResponse {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(ApiErrorCode.NOT_FOUND.code)

        if (deck.userId != userId) throw ForbiddenException(ApiErrorCode.FORBIDDEN.code)

        return deck.toResponse()
    }

    fun updateDeck(
        deckId: UUID,
        userId: UUID,
        req: CreateDeckRequest,
    ): DeckResponse {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(ApiErrorCode.NOT_FOUND.code)

        if (deck.userId != userId) throw ForbiddenException(ApiErrorCode.FORBIDDEN.code)

        if (!deckRepository.existsByUserIdAndNameAndId(userId, req.name, deckId)) {
            throw ConflictException(ApiErrorCode.CONFLICT.code)
        }

        val updatedDeck =
            deck.copy(
                name = req.name,
                description = req.description,
            )
        return deckRepository.update(updatedDeck).toResponse()
    }

    fun deleteDeck(
        deckId: UUID,
        userId: UUID,
    ) {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(ApiErrorCode.NOT_FOUND.code)

        if (deck.userId != userId) throw ForbiddenException(ApiErrorCode.FORBIDDEN.code)

        deckRepository.delete(deckId)
    }
}
