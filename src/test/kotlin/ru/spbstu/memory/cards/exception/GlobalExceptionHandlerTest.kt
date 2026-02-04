package ru.spbstu.memory.cards.exception

import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Path
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription
import ru.spbstu.memory.cards.exception.domain.ConflictException
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.LimitExceededException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.exception.domain.ValidationDomainException

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()

    @Test
    fun handleNotFound_shouldReturn404AndNotFoundCode_whenNotFoundException() {
        val ex = NotFoundException(code = ApiErrorCode.USER_NOT_FOUND)

        val result = handler.handleNotFound(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.USER_NOT_FOUND.code)
        assertThat(result.body?.message).isEqualTo(ApiErrorDescription.NOT_FOUND.description)
    }

    @Test
    fun handleNotFound_shouldReturnDeckCode_whenDeckNotFound() {
        val ex = NotFoundException(code = ApiErrorCode.DECK_NOT_FOUND)

        val result = handler.handleNotFound(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.DECK_NOT_FOUND.code)
        assertThat(result.body?.message).isEqualTo(ApiErrorDescription.NOT_FOUND.description)
    }

    @Test
    fun handleConflict_shouldReturn409AndConflictCode_whenConflictException() {
        val ex = ConflictException(code = ApiErrorCode.EMAIL_CONFLICT)

        val result = handler.handleConflict(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.EMAIL_CONFLICT.code)
        assertThat(result.body?.message).isEqualTo(ApiErrorDescription.CONFLICT.description)
    }

    @Test
    fun handleUnauthorized_shouldReturn401_whenUnauthorizedException() {
        val ex = UnauthorizedException(ApiErrorDescription.UNAUTHORIZED.description)

        val result = handler.handleUnauthorized(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.UNAUTHORIZED.code)
        assertThat(result.body?.message).isEqualTo(ApiErrorDescription.UNAUTHORIZED.description)
    }

    @Test
    fun handleForbidden_shouldReturn403_whenForbiddenException() {
        val ex = ForbiddenException()

        val result = handler.handleForbidden(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.FORBIDDEN.code)
        assertThat(result.body?.message).isEqualTo(ApiErrorDescription.FORBIDDEN.description)
    }

    @Test
    fun handleForbidden_shouldReturn403_whenAccessDeniedException() {
        val ex = AccessDeniedException("denied")

        val result = handler.handleForbidden(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.FORBIDDEN.code)
        assertThat(result.body?.message).isEqualTo(ApiErrorDescription.FORBIDDEN.description)
    }

    @Test
    fun handleLimitExceeded_shouldReturn422WithCustomMessage_whenLimitExceededException() {
        val ex =
            LimitExceededException(
                code = ApiErrorCode.CARD_LIMIT,
                message = "Max 100 cards",
                details = mapOf("limit" to 100),
            )

        val result = handler.handleLimitExceeded(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.CARD_LIMIT.code)
        assertThat(result.body?.message).isEqualTo("Max 100 cards")
        assertThat(result.body?.details).isEqualTo(mapOf("limit" to 100))
    }

    @Test
    fun handleBadRequest_shouldReturn400_whenValidationDomainExceptionWithDetails() {
        val ex =
            ValidationDomainException(
                message = "Invalid",
                details = mapOf("field" to "error"),
            )

        val result = handler.handleBadRequest(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.VALIDATION_ERROR.code)
        assertThat(result.body?.message).isEqualTo(ApiErrorDescription.VALIDATION_ERROR.description)
        assertThat(result.body?.details).isEqualTo(mapOf("field" to "error"))
    }

    @Test
    fun handleAny_shouldReturn500_whenUnhandledThrowable() {
        val ex = RuntimeException("unexpected")

        val result = handler.handleAny(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.INTERNAL_ERROR.code)
        assertThat(result.body?.message).isEqualTo(ApiErrorDescription.INTERNAL_ERROR.description)
    }

    @Test
    fun handleUnauthorized_shouldReturn401_whenAuthenticationException() {
        val ex =
            object : AuthenticationException("auth failed") {}

        val result = handler.handleUnauthorized(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.UNAUTHORIZED.code)
        assertThat(result.body?.message).isEqualTo(ApiErrorDescription.UNAUTHORIZED.description)
    }

    @Test
    fun handleMethodArgumentNotValid_shouldReturnSingleString_whenSingleErrorPerField() {
        val bindingResult = mock<BindingResult>()
        val ex = mock<MethodArgumentNotValidException>()

        whenever(ex.bindingResult).thenReturn(bindingResult)
        whenever(bindingResult.fieldErrors).thenReturn(
            listOf(
                FieldError("obj", "login", "bad login"),
            ),
        )

        val result = handler.handleMethodArgumentNotValid(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.VALIDATION_ERROR.code)
        assertThat(result.body?.details).isEqualTo(mapOf("login" to "bad login"))
    }

    @Test
    fun handleMethodArgumentNotValid_shouldReturnList_whenMultipleErrorsPerField() {
        val bindingResult = mock<BindingResult>()
        val ex = mock<MethodArgumentNotValidException>()

        whenever(ex.bindingResult).thenReturn(bindingResult)
        whenever(bindingResult.fieldErrors).thenReturn(
            listOf(
                FieldError("obj", "password", "too short"),
                FieldError("obj", "password", "must contain digit"),
            ),
        )

        val result = handler.handleMethodArgumentNotValid(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.VALIDATION_ERROR.code)
        assertThat(result.body?.details).isEqualTo(
            mapOf("password" to listOf("too short", "must contain digit")),
        )
    }

    @Test
    fun handleBindException_shouldReturnSingleString_whenSingleErrorPerField() {
        val bindingResult = mock<BindingResult>()
        val ex = mock<BindException>()

        whenever(ex.bindingResult).thenReturn(bindingResult)
        whenever(bindingResult.fieldErrors).thenReturn(
            listOf(FieldError("obj", "email", "bad email")),
        )

        val result = handler.handleBindException(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.VALIDATION_ERROR.code)
        assertThat(result.body?.details).isEqualTo(mapOf("email" to "bad email"))
    }

    @Test
    fun handleBindException_shouldReturnList_whenMultipleErrorsPerField() {
        val bindingResult = mock<BindingResult>()
        val ex = mock<BindException>()

        whenever(ex.bindingResult).thenReturn(bindingResult)
        whenever(bindingResult.fieldErrors).thenReturn(
            listOf(
                FieldError("obj", "email", "bad email"),
                FieldError("obj", "email", "already used"),
            ),
        )

        val result = handler.handleBindException(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(result.body?.details).isEqualTo(
            mapOf("email" to listOf("bad email", "already used")),
        )
    }

    @Test
    fun handleConstraintViolation_shouldUseProvidedPathAndMessage_whenPresent() {
        val violation = mock<ConstraintViolation<Any>>()
        val path = mock<Path>()

        whenever(path.toString()).thenReturn("field")
        whenever(violation.propertyPath).thenReturn(path)
        whenever(violation.message).thenReturn("must not be blank")

        val ex = ConstraintViolationException(setOf(violation))

        val result = handler.handleConstraintViolation(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(result.body?.code).isEqualTo(ApiErrorCode.VALIDATION_ERROR.code)
        assertThat(result.body?.details).isEqualTo(mapOf("field" to "must not be blank"))
    }

    @Test
    fun handleConstraintViolation_shouldFallbackToDefaults_whenPathAndMessageNull() {
        val violation = mock<ConstraintViolation<Any>>()

        whenever(violation.propertyPath).thenReturn(null) // ?: "value"
        whenever(violation.message).thenReturn(null) // ?: "invalid"

        val ex = ConstraintViolationException(setOf(violation))

        val result = handler.handleConstraintViolation(ex)

        assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(result.body?.details).isEqualTo(mapOf("value" to "invalid"))
    }

    @Suppress("UNUSED_PARAMETER")
    private class TestController {
        fun method(arg: String) {}
    }
}
