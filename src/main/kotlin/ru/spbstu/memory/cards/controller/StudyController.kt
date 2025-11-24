package ru.spbstu.memory.cards.controller

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.spbstu.memory.cards.dto.request.StudyAnswerRequest
import ru.spbstu.memory.cards.dto.response.CardResponse
import ru.spbstu.memory.cards.dto.response.StudyAnswerResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.card.CardService
import ru.spbstu.memory.cards.service.study.StudyService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/study")
class StudyController(
    private val cardService: CardService,
    private val studyService: StudyService,
) {
    @GetMapping("/{deckId}/cards")
    fun getCardsForStudy(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @PathVariable deckId: UUID,
    ): List<CardResponse> {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)

        return cardService.getAllCards(deckId, userId)
    }

    @PostMapping("/{deckId}/answer")
    fun processCardAnswer(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @PathVariable deckId: UUID,
        @Valid @RequestBody req: StudyAnswerRequest,
    ): StudyAnswerResponse {
        val userId = principal?.getId() ?: throw UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)

        return studyService.processStudyAnswer(deckId, userId, req)
    }
}
