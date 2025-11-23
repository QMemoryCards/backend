package ru.spbstu.memory.cards.exception

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import ru.spbstu.memory.cards.exception.api.ApiError
import ru.spbstu.memory.cards.exception.api.ApiErrorCode
import ru.spbstu.memory.cards.exception.api.ApiErrorDescription
import ru.spbstu.memory.cards.exception.domain.ConflictException
import ru.spbstu.memory.cards.exception.domain.ForbiddenException
import ru.spbstu.memory.cards.exception.domain.LimitExceededException
import ru.spbstu.memory.cards.exception.domain.NotFoundException
import ru.spbstu.memory.cards.exception.domain.UnauthorizedException
import ru.spbstu.memory.cards.exception.domain.ValidationDomainException

@RestControllerAdvice
class GlobalExceptionHandler {
    private fun error(
        status: HttpStatus,
        code: ApiErrorCode,
        description: ApiErrorDescription,
        details: Map<String, Any?>? = null,
    ): ResponseEntity<ApiError> =
        ResponseEntity.status(status)
            .body(
                ApiError(
                    code = code.code,
                    message = description.description,
                    details = details,
                ),
            )

    private fun errorMessage(
        status: HttpStatus,
        code: ApiErrorCode,
        message: String,
        details: Map<String, Any?>? = null,
    ): ResponseEntity<ApiError> =
        ResponseEntity.status(status)
            .body(
                ApiError(
                    code = code.code,
                    message = message,
                    details = details,
                ),
            )

    private fun validation(details: Map<String, Any?>?): ResponseEntity<ApiError> =
        error(
            status = HttpStatus.BAD_REQUEST,
            code = ApiErrorCode.VALIDATION_ERROR,
            description = ApiErrorDescription.VALIDATION_ERROR,
            details = details,
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val details =
            ex.bindingResult.fieldErrors
                .groupBy({ it.field }, { it.defaultMessage ?: "invalid" })
                .mapValues { (_, v) -> if (v.size == 1) v.first() else v }
        return validation(details)
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(ex: BindException): ResponseEntity<ApiError> {
        val details =
            ex.bindingResult.fieldErrors
                .groupBy({ it.field }, { it.defaultMessage ?: "invalid" })
                .mapValues { (_, v) -> if (v.size == 1) v.first() else v }
        return validation(details)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ApiError> {
        val details =
            ex.constraintViolations.associate {
                (it.propertyPath?.toString() ?: "value") to (it.message ?: "invalid")
            }
        return validation(details)
    }

    @ExceptionHandler(
        MissingServletRequestParameterException::class,
        MethodArgumentTypeMismatchException::class,
        HttpMessageNotReadableException::class,
        ValidationDomainException::class,
    )
    fun handleBadRequest(ex: Exception): ResponseEntity<ApiError> {
        val details = (ex as? ValidationDomainException)?.details
        return error(
            status = HttpStatus.BAD_REQUEST,
            code = ApiErrorCode.VALIDATION_ERROR,
            description = ApiErrorDescription.VALIDATION_ERROR,
            details = details,
        )
    }

    @ExceptionHandler(UnauthorizedException::class, AuthenticationException::class)
    fun handleUnauthorized(ex: Exception): ResponseEntity<ApiError> =
        error(
            status = HttpStatus.UNAUTHORIZED,
            code = ApiErrorCode.UNAUTHORIZED,
            description = ApiErrorDescription.UNAUTHORIZED,
        )

    @ExceptionHandler(ForbiddenException::class, AccessDeniedException::class)
    fun handleForbidden(ex: Exception): ResponseEntity<ApiError> =
        error(
            status = HttpStatus.FORBIDDEN,
            code = ApiErrorCode.FORBIDDEN,
            description = ApiErrorDescription.FORBIDDEN,
        )

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ApiError> =
        error(
            status = HttpStatus.NOT_FOUND,
            code = ex.code,
            description = ApiErrorDescription.NOT_FOUND,
        )

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException): ResponseEntity<ApiError> =
        error(
            status = HttpStatus.CONFLICT,
            code = ex.code,
            description = ApiErrorDescription.CONFLICT,
        )

    @ExceptionHandler(LimitExceededException::class)
    fun handleLimitExceeded(ex: LimitExceededException): ResponseEntity<ApiError> =
        errorMessage(
            status = HttpStatus.UNPROCESSABLE_ENTITY,
            code = ex.code,
            message = ex.message ?: ApiErrorDescription.LIMIT_EXCEEDED.description,
            details = ex.details,
        )

    @ExceptionHandler(Throwable::class)
    fun handleAny(ex: Throwable): ResponseEntity<ApiError> {
        LoggerFactory.getLogger(javaClass).error("Unhandled exception", ex)
        return error(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = ApiErrorCode.INTERNAL_ERROR,
            description = ApiErrorDescription.INTERNAL_ERROR,
        )
    }
}
