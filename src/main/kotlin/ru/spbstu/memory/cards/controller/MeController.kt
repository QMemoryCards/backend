package ru.spbstu.memory.cards.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.spbstu.memory.cards.dto.response.UserResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/v1/users/me")
class MeController {
    @GetMapping
    fun me(
        @AuthenticationPrincipal principal: AppUserDetails?,
    ): UserResponse =
        principal?.let {
            UserResponse(
                id = it.getId().toString(),
                email = it.getEmail(),
                login = it.username,
                createdAt = it.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            )
        } ?: throw UnauthorizedException(ApiErrorCode.UNAUTHORIZED.code)
}
