package ru.spbstu.memory.cards.service.share

import org.springframework.stereotype.Service
import ru.spbstu.memory.cards.dto.request.CreateDeckRequest
import ru.spbstu.memory.cards.dto.request.ImportSharedDeckRequest
import ru.spbstu.memory.cards.dto.response.DeckResponse
import ru.spbstu.memory.cards.dto.response.DeckShareResponse
import ru.spbstu.memory.cards.dto.response.ShareResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.persistence.CardRepository
import ru.spbstu.memory.cards.persistence.DeckRepository
import ru.spbstu.memory.cards.persistence.DeckSharesRepository
import ru.spbstu.memory.cards.persistence.mapper.toResponse
import ru.spbstu.memory.cards.service.deck.DeckService
import java.util.UUID

@Service
class DeckShareService(
    private val deckService: DeckService,
    private val shareRepository: DeckSharesRepository,
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
) {
    fun generateShareToken(
        deckId: UUID,
        userId: UUID,
    ): ShareResponse {
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(code = ApiErrorCode.DECK_NOT_FOUND)
        if (deck.userId != userId) throw ForbiddenException(ApiErrorDescription.FORBIDDEN.description)

        val token = UUID.randomUUID()
        shareRepository.saveToken(token, deckId)

        return ShareResponse(token = token.toString(), url = "/api/v1/share/$token")
    }

    fun getSharedDeck(token: UUID): DeckShareResponse {
        val deckId =
            shareRepository.findByToken(token)
                ?: throw NotFoundException(code = ApiErrorCode.TOKEN_NOT_FOUND)
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(code = ApiErrorCode.DECK_NOT_FOUND)
        return DeckShareResponse(
            name = deck.name,
            description = deck.description,
            cardCount = deck.cardsCount,
        )
    }

    fun importSharedDeck(
        token: UUID,
        userId: UUID,
        req: ImportSharedDeckRequest?,
    ): DeckResponse {
        val deckId =
            shareRepository.findByToken(token)
                ?: throw NotFoundException(code = ApiErrorCode.TOKEN_NOT_FOUND)
        val deck =
            deckRepository.findById(deckId)
                ?: throw NotFoundException(code = ApiErrorCode.DECK_NOT_FOUND)
        val deckReq =
            if (req != null) {
                CreateDeckRequest(
                    name = req.newName,
                    description = req.newDescription,
                )
            } else {
                CreateDeckRequest(
                    name = deck.name,
                    description = deck.description,
                )
            }
        val newDeck = deckService.createDeck(userId, deckReq)
        val cardsCopied = cardRepository.copyAllToDeck(sourceDeckId = deckId, targetDeckId = newDeck.id)
        val deckV =
            deckRepository.findById(newDeck.id)
                ?: throw NotFoundException(code = ApiErrorCode.DECK_NOT_FOUND)
        val updatedDeck = deckV.copy(cardsCount = cardsCopied)
        deckRepository.update(updatedDeck)
        return updatedDeck.toResponse()
    }
}
