package ru.spbstu.memory.cards.validation.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import ru.spbstu.memory.cards.validation.PasswordValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [PasswordValidator::class])
annotation class ValidPassword(
    val message: String = "invalid password format",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
