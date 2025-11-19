package ru.spbstu.memory.cards.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.spbstu.memory.cards.dto.request.UpdatePasswordRequest
import ru.spbstu.memory.cards.dto.request.UpdateUserRequest
import ru.spbstu.memory.cards.dto.response.UserResponse
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.service.auth.model.AppUserDetails
import ru.spbstu.memory.cards.service.user.UserService
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/v1/users/me")
class MeController(
    private val userService: UserService,
) {
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

    @PutMapping
    fun updateProfile(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @Valid @RequestBody body: UpdateUserRequest,
    ): UserResponse {
        val userDetails = principal ?: throw UnauthorizedException(ApiErrorCode.UNAUTHORIZED.code)
        return userService.updateProfile(userDetails.getId(), body)
    }

    @PutMapping("/password")
    fun updatePassword(
        @AuthenticationPrincipal principal: AppUserDetails?,
        @Valid @RequestBody body: UpdatePasswordRequest,
    ) {
        val userDetails = principal ?: throw UnauthorizedException(ApiErrorCode.UNAUTHORIZED.code)
        userService.updatePassword(userDetails.getId(), body)
    }

    @DeleteMapping
    fun deleteAccount(
        @AuthenticationPrincipal principal: AppUserDetails?,
        request: HttpServletRequest,
    ) {
        val userDetails = principal ?: throw UnauthorizedException(ApiErrorCode.UNAUTHORIZED.code)

        userService.deleteUser(userDetails.getId())
        request.getSession(false)?.invalidate()
    }
}
