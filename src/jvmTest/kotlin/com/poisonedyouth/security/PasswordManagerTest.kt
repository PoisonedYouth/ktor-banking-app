package com.poisonedyouth.security

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

class PasswordManagerTest {

    @Test
    fun `validatePassword throws exception if password is too short`() {
        // given
        val password = "too short"

        // when
        val actual = PasswordManager.validatePassword(password)

        // then
        assertThat(actual.second).isEqualTo("Password must be at minimum '16' characters.")

    }

    @Test
    fun `validatePassword throws exception if password has no uppercase character`() {
        // given
        val password = "tttttttttttttttt"

        // when
        val actual = PasswordManager.validatePassword(password)

        // then
        assertThat(actual.second).isEqualTo("Password must contain at minimum one lowercase, one uppercase, one special character and one digit.")

    }

    @Test
    fun `validatePassword throws exception if password has no lowercase character`() {
        // given
        val password = "TTTTTTTTTTTTTTTT"

        // when
        val actual = PasswordManager.validatePassword(password)

        // then
        assertThat(actual.second).isEqualTo("Password must contain at minimum one lowercase, one uppercase, one special character and one digit.")

    }

    @Test
    fun `validatePassword throws exception if password has no number character`() {
        // given
        val password = "TTTTTTTTTTTTTTTt"

        // when
        val actual = PasswordManager.validatePassword(password)

        // when + then
        assertThat(actual.second).isEqualTo("Password must contain at minimum one lowercase, one uppercase, one special character and one digit.")

    }

    @Test
    fun `validatePassword throws exception if password has no special character`() {
        // given
        val password = "TTTTTTTTTTTTTT&t"

        // when
        val actual = PasswordManager.validatePassword(password)

        // then
        assertThat(actual.second).isEqualTo("Password must contain at minimum one lowercase, one uppercase, one special character and one digit.")

    }

    @Test
    fun `validatePassword returns true for correct password`() {
        // given
        val password = "Ta1&tudol3lal54e"

        // when
        val actual = PasswordManager.validatePassword(password)

        // then
        assertThat(actual.first).isTrue
    }

    @Test
    fun `generatePassword returns matching password`() {
        // given + when
        val actual = PasswordManager.generatePassword()

        // then
        assertThat(PasswordManager.validatePassword(actual).first).isTrue
    }

}