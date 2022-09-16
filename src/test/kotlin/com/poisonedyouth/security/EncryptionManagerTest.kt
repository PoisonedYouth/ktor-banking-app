package com.poisonedyouth.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EncryptionManagerTest {

    @Test
    fun `encrypt and decrypt are working for correct data`() {
        // given
        val password = "Ta1&tudol3lal54e"

        // when
        val result = EncryptionManager.encrypt(password)

        // then
        assertThat(EncryptionManager.decrypt(result)).isEqualTo(password)
    }

    @Test
    fun `encrypt and decrypt are working for incorrect data`() {
        // given
        val password = "password"

        // when
        val result = EncryptionManager.encrypt("otherPassword")

        // then
        assertThat(
            EncryptionManager.decrypt(result)
        ).isNotEqualTo(password)
    }
}