package com.poisonedyouth.application

import com.poisonedyouth.application.ErrorCode.PERSISTENCE_ERROR
import com.poisonedyouth.domain.User
import com.poisonedyouth.persistence.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

interface UserService {

    fun createUser(userDto: UserDto): ApiResult<UUID>
}

private const val BIRTH_DATE_FORMAT = "dd.MM.yyyy"

class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {
    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)

    override fun createUser(userDto: UserDto): ApiResult<UUID> {
        logger.info("Start creation of user '$userDto'...")
        val user = try {
            userDto.toUser()
        } catch (e: InvalidInputException) {
            logger.error("Unable to map given dto '$userDto' to domain object.", e)
            return ApiResult.Failure(
                ErrorCode.MAPPING_ERROR,
                e.message ?: "Undefined error during mapping occurred."
            )
        }
        return try {
            ApiResult.Success(userRepository.save(user = user).userId)
        } catch (e: Exception) {
            logger.error("Unable to create user '$userDto' in database.", e)
            ApiResult.Failure(PERSISTENCE_ERROR, e.message ?: "Undefined error during persistence occurred.")
        }

    }

    private fun parseBirthdate(birthdate: String): LocalDate {
        try {
            return LocalDate.parse(birthdate, DateTimeFormatter.ofPattern(BIRTH_DATE_FORMAT))
        } catch (e: DateTimeParseException) {
            throw InvalidInputException("Birthdate '$birthdate' is not parsable using pattern '$BIRTH_DATE_FORMAT'.", e)
        }
    }

    private fun UserDto.toUser() = try {
        User(
            firstName = this.firstName,
            lastName = this.lastName,
            birthdate = parseBirthdate(this.birthDate),
            password = this.password
        )
    } catch (e: IllegalArgumentException) {
        throw InvalidInputException("Given UserDto '$this' is not valid.", e)
    }
}