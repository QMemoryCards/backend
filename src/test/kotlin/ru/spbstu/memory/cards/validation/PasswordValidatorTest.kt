package ru.spbstu.memory.cards.validation

import jakarta.validation.ConstraintValidatorContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PasswordValidatorTest {
    private lateinit var validator: PasswordValidator
    private lateinit var context: ConstraintValidatorContext

    @BeforeEach
    fun setUp() {
        validator = PasswordValidator()
        validator.initialize(mock())
        context = mock()
        whenever(context.buildConstraintViolationWithTemplate(any())).thenReturn(mock())
    }

    @ParameterizedTest
    @NullAndEmptySource
    fun isValid_shouldReturnFalse_whenNullOrEmpty(value: String?) {
        assertThat(validator.isValid(value, context)).isFalse()
    }

    @Test
    fun isValid_shouldReturnTrue_whenValidPassword() {
        assertThat(validator.isValid("Password1!", context)).isTrue()
    }

    @ParameterizedTest(name = "valid: {0}")
    @ValueSource(strings = ["Abcd1234@", "XyZ99!@#", "MyP@ssw0rd"])
    fun isValid_shouldReturnTrue_whenVariousValidPasswords(password: String) {
        assertThat(validator.isValid(password, context)).isTrue()
    }

    @Test
    fun isValid_shouldReturnFalse_whenLengthLessThan8() {
        assertThat(validator.isValid("Abc12!x", context)).isFalse()
    }

    @Test
    fun isValid_shouldReturnTrue_whenLength8() {
        assertThat(validator.isValid("Abcd123!", context)).isTrue()
    }

    @Test
    fun isValid_shouldReturnFalse_whenLengthGreaterThan64() {
        val password = "A" + "b".repeat(62) + "1!"
        assertThat(password.length).isEqualTo(65)
        assertThat(validator.isValid(password, context)).isFalse()
    }

    @Test
    fun isValid_shouldReturnFalse_whenNoUppercase() {
        assertThat(validator.isValid("password1!", context)).isFalse()
    }

    @Test
    fun isValid_shouldReturnFalse_whenNoLowercase() {
        assertThat(validator.isValid("PASSWORD1!", context)).isFalse()
    }

    @Test
    fun isValid_shouldReturnFalse_whenNoDigit() {
        assertThat(validator.isValid("Password!!", context)).isFalse()
    }

    @Test
    fun isValid_shouldReturnFalse_whenNoSpecialCharacter() {
        assertThat(validator.isValid("Password12", context)).isFalse()
    }

    @ParameterizedTest(name = "disallowed: {0}")
    @ValueSource(strings = ["Pass word1!", "Pass\tword1!", "Пароль123!", "A\u0000bcd123!"])
    fun isValid_shouldReturnFalse_whenContainsDisallowedCharacters(password: String) {
        assertThat(validator.isValid(password, context)).isFalse()
    }

    @Test
    fun isValid_shouldReturnFalse_whenBlankString() {
        assertThat(validator.isValid("   ", context)).isFalse()
    }
}
