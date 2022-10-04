package com.poisonedyouth.security

import java.security.SecureRandom

private const val MINIMUM_PASSWORD_LENGTH = 16
private const val PASSWORD_REGEX_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$"

object PasswordManager {

    private const val lowerCaseCharacter: String = "abcdefghijklmnopqrstuvwxyz"
    private const val upperCaseCharacter: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val numbers: String = "0123456789"
    private const val specialCharacter: String = "@$!%*?&"

    fun generatePassword(): String {

        val resultValues = listOf(lowerCaseCharacter, upperCaseCharacter, numbers, specialCharacter)
        var i = 0


        val rnd = SecureRandom.getInstance("SHA1PRNG")
        val sb = StringBuilder(MINIMUM_PASSWORD_LENGTH)

        repeat (MINIMUM_PASSWORD_LENGTH) {
            val type = it % 4
            val typeCharacters = resultValues[type]
            sb.append(
                typeCharacters[rnd.nextInt(typeCharacters.length-1)]
            )
            i++
        }

        return sb.toString()
    }

    fun validatePassword(password: String): Pair<Boolean, String> {
        return if (password.length < MINIMUM_PASSWORD_LENGTH) {
            Pair(false, "Password must be at minimum '$MINIMUM_PASSWORD_LENGTH' characters.")
        } else if (!password.matches(Regex(PASSWORD_REGEX_PATTERN))) {
            Pair(
                false,
                "Password must contain at minimum one lowercase, one uppercase, one special character and one digit."
            )
        } else {
            Pair(true, "")
        }
    }
}