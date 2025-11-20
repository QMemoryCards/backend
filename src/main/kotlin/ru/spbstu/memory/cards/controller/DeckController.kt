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
import ru.spbstu.memory.cards.dto.request.CreateDeckRequest
import ru.spbstu.memory.cards.dto.response.DeckResponse
import ru.spbstu.memory.cards.dto.response.PageResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.deck.DeckService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/decks")
class DeckController(
    private val deckService: DeckService,
) {
    @GetMapping
    fun getDecks(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PageResponse<DeckResponse> {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)
        return deckService.getDecks(userId, page, size)
    }

    @PostMapping
    fun createDeck(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @Valid @RequestBody request: CreateDeckRequest,
    ): DeckResponse {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)
        return deckService.createDeck(userId, request)
    }

    @GetMapping("/{deckId}")
    fun getDeck(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @PathVariable deckId: UUID,
    ): DeckResponse {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)
        return deckService.getDeck(deckId, userId)
    }

    @PutMapping("/{deckId}")
    fun updateDeck(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @PathVariable deckId: UUID,
        @Valid @RequestBody request: CreateDeckRequest,
    ): DeckResponse {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)
        return deckService.updateDeck(deckId, userId, request)
    }

    @DeleteMapping("/{deckId}")
    fun deleteDeck(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @PathVariable deckId: UUID,
    ) {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)
        deckService.deleteDeck(deckId, userId)
    }
}
