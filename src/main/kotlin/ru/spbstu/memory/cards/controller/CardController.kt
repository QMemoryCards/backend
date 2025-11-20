package ru.spbstu.memory.cards.controller

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.spbstu.memory.cards.dto.request.CreateCardRequest
import ru.spbstu.memory.cards.dto.response.CardResponse
import ru.spbstu.memory.cards.dto.response.PageResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.card.CardService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/decks/{deckId}/cards")
class CardController(
    private val cardService: CardService,
) {
    @GetMapping
    fun getCards(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @PathVariable deckId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PageResponse<CardResponse> {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)
        return cardService.getCards(deckId, userId, page, size)
    }

    @PostMapping
    fun createCard(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @PathVariable deckId: UUID,
        @Valid @RequestBody request: CreateCardRequest,
    ): CardResponse {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)
        return cardService.createCard(deckId, userId, request)
    }

    @PutMapping("/{cardId}")
    fun updateCard(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @PathVariable deckId: UUID,
        @PathVariable cardId: UUID,
        @Valid @RequestBody request: CreateCardRequest,
    ): CardResponse {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)
        return cardService.updateCard(deckId, cardId, userId, request)
    }

    @DeleteMapping("/{cardId}")
    fun deleteCard(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @PathVariable deckId: UUID,
        @PathVariable cardId: UUID,
    ) {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)
        cardService.deleteCard(deckId, cardId, userId)
    }
}
