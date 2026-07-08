package com.conecta_voluntariado_tfg

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class ValidationUtilsTest {

    @Test
    fun emailWithAtSymbol_isValid() {
        val email = "usuario@gmail.com"

        val result = ValidationUtils.isValidEmail(email)

        assertTrue(result)
    }

    @Test
    fun emailWithoutAtSymbol_isNotValid() {
        val email = "usuariogmail.com"

        val result = ValidationUtils.isValidEmail(email)

        assertFalse(result)
    }

    @Test
    fun passwordWithSixCharacters_isValid() {
        val pass = "123456"

        val result = ValidationUtils.isValidPassword(pass)

        assertTrue(result)
    }

    @Test
    fun passwordWithLessThanSixCharacters_isNotValid() {
        val pass = "12345"

        val result = ValidationUtils.isValidPassword(pass)

        assertFalse(result)
    }

}