package ru.spbstu.memory.cards.controller

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.spbstu.memory.cards.dto.request.ImportSharedDeckRequest
import ru.spbstu.memory.cards.dto.response.DeckResponse
import ru.spbstu.memory.cards.dto.response.DeckShareResponse
import ru.spbstu.memory.cards.dto.response.ShareResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.share.DeckShareService
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class ShareController(private val shareService: DeckShareService) {
    @PostMapping("/decks/{deckId}/share")
    fun shareDeck(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @PathVariable deckId: UUID,
    ): ShareResponse {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)

        return shareService.generateShareToken(deckId, userId)
    }

    @GetMapping("/share/{token}")
    fun getSharedDeck(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @PathVariable token: UUID,
    ): DeckShareResponse {
        principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)

        return shareService.getSharedDeck(token)
    }

    @PostMapping("/share/{token}/import")
    fun importDeck(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @PathVariable token: UUID,
        @Valid @RequestBody(required = false) req: ImportSharedDeckRequest?,
    ): DeckResponse {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)
        return shareService.importSharedDeck(token, userId, req)
    }
}
