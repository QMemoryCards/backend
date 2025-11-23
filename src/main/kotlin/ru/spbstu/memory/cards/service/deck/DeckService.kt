package ru.spbstu.memory.cards.service.deck

import org.springframework.stereotype.Service
import ru.spbstu.memory.cards.dto.request.CreateDeckRequest
import ru.spbstu.memory.cards.dto.response.DeckResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription
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
    companion object {
        const val DECK_LIMIT = 30
    }

    fun createDeck(
        userId: UUID,
        req: CreateDeckRequest,
    ): DeckResponse {
        val count = deckRepository.countByUserId(userId)
        if (count >= DECK_LIMIT) {
            throw LimitExceededException(code = ApiErrorCode.DECK_LIMIT)
        }

        if (deckRepository.existsByUserIdAndName(userId, req.name)) {
            throw ConflictException(code = ApiErrorCode.DECK_CONFLICT)
        }

        val deck = deckRepository.saveNew(userId, req.name, req.description)
        return deck.toResponse()
    }

    fun getDecksPaginated(
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
                ?: throw NotFoundException(code = ApiErrorCode.DECK_NOT_FOUND)
        if (deck.userId != userId) throw ForbiddenException(ApiErrorDescription.FORBIDDEN.description)

        return deck.toResponse()
    }

    fun updateDeck(
        deckId: UUID,
        userId: UUID,
        req: CreateDeckRequest,
    ): DeckResponse {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(code = ApiErrorCode.DECK_NOT_FOUND)

        if (deck.userId != userId) throw ForbiddenException(ApiErrorDescription.FORBIDDEN.description)

        if (!deckRepository.existsByUserIdAndNameAndId(userId, req.name, deckId)) {
            throw ConflictException(code = ApiErrorCode.DECK_CONFLICT)
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
                ?: throw NotFoundException(code = ApiErrorCode.DECK_NOT_FOUND)

        if (deck.userId != userId) throw ForbiddenException(ApiErrorDescription.FORBIDDEN.description)

        deckRepository.delete(deckId)
    }
}
