package ru.spbstu.memory.cards.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ru.spbstu.memory.cards.validation.annotation.ValidPassword

class PasswordValidator : ConstraintValidator<ValidPassword, String> {
    private val allowedRegex =
        Regex(
            pattern = "^[A-Za-z0-9~`!@#\$%^&*()_\\-+=\\{\\[}\\]\\\\:;\"'<,>\\.\\?/]{8,64}\$",
        )

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null) return false

        if (!allowedRegex.matches(value)) {
            return false
        }

        val hasUpper = value.any { it.isUpperCase() && it in 'A'..'Z' }
        val hasLower = value.any { it.isLowerCase() && it in 'a'..'z' }
        val hasDigit = value.any { it.isDigit() }
        val hasSpecial = value.any { it in specialChars }

        return hasUpper && hasLower && hasDigit && hasSpecial
    }

    companion object {
        private val specialChars: Set<Char> =
            setOf(
                '~', '`', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '-', '+', '=',
                '{', '[', '}', '}',
                '|', '\\', ':', ';', '"', '\'', '<', ',', '>', '.', '?', '/',
            )
    }
}
