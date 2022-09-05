package com.poisonedyouth.application

import com.poisonedyouth.domain.User
import com.poisonedyouth.persistence.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

interface UserService {

    fun createUser(userDto: UserDto): ApiResult<UUID>

    fun deleteUser(userId: String?): ApiResult<UUID>

    fun updateUser(userDto: UserDto): ApiResult<UUID>
}

private const val BIRTH_DATE_FORMAT = "dd.MM.yyyy"

class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {
    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)

    override fun createUser(userDto: UserDto): ApiResult<UUID> {
        logger.info("Start creation of user '$userDto'.")
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
            val persistedUser = userRepository.save(user = user)
            logger.info("Successfully created user '$persistedUser'.")
            ApiResult.Success(persistedUser.userId)

        } catch (e: Exception) {
            logger.error("Unable to create user '$userDto' in database.", e)
            ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.message ?: "Undefined error during persistence occurred.")
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
        val user = User(
            firstName = this.firstName,
            lastName = this.lastName,
            birthdate = parseBirthdate(this.birthdate),
            password = this.password
        )
        if (this.userId != null) {
            user.copy(
                userId = this.userId
            )
        } else {
            user
        }
    } catch (e: IllegalArgumentException) {
        throw InvalidInputException("Given UserDto '$this' is not valid.", e)
    }

    override fun deleteUser(userId: String?): ApiResult<UUID> {
        logger.info("Start deleting user with userId'$userId'.")
        val userIdResolved = try {
            UUID.fromString(userId)
        } catch (e: IllegalArgumentException) {
            logger.error("Given userId '$userId' is not valid.")
            return ApiResult.Failure(ErrorCode.MAPPING_ERROR, "Given userId '$userId' is not valid.")
        }
        val user = try {
            val existingUser = userRepository.findByUserId(userIdResolved)
            if (existingUser == null) {
                logger.error("User with userId '$userId' not found.")
                return ApiResult.Failure(ErrorCode.USER_NOT_FOUND, "User with userId '$userId' not found.")
            }
            existingUser
        } catch (e: Exception) {
            logger.error("Unable to find user with userId '$userId' in database.", e)
            return ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
        }
        return try {
            userRepository.delete(user)
            logger.info("Successfully deleted user '$user'.")
            ApiResult.Success(user.userId)
        } catch (e: Exception) {
            logger.error("Unable to delete user with userId '$userId' from database.", e)
            ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
        }
    }

    override fun updateUser(userDto: UserDto): ApiResult<UUID> {
        logger.info("Start updating user '$userDto'")
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
            userRepository.save(user)
            logger.info("Successfully updated user '$user'.")
            ApiResult.Success(user.userId)
        } catch (e: Exception) {
            logger.error("Unable to update user '$userDto' to database.", e)
            ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.message ?: "Undefined error during persistence occurred.")
        }
    }

}

fun Exception.getErrorMessage(): String {
    return message ?: return "Unexpected error occurred."
}