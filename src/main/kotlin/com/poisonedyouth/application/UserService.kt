package com.poisonedyouth.application

import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.User
import com.poisonedyouth.persistence.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.*

interface UserService {

    fun createUser(userDto: UserDto): ApiResult<UUID>
    fun deleteUser(userId: String?): ApiResult<UUID>
    fun updateUser(userDto: UserDto): ApiResult<UUID>
    fun updatePassword(userPasswordChangeDto: UserPasswordChangeDto): ApiResult<UUID>
    fun findUserByUserId(userId: String?): ApiResult<UserOverviewDto>
}

private const val BIRTH_DATE_FORMAT = "dd.MM.yyyy"
private const val TIME_STAMP_FORMAT = "dd.MM.yyyy HH:mm:ss"

class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {
    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)

    @SuppressWarnings("TooGenericExceptionCaught") // It's intended to catch all exceptions in service
    override fun createUser(userDto: UserDto): ApiResult<UUID> {
        logger.info("Start creation of user '$userDto'.")
        val user = try {
            userDto.toUser()
        } catch (e: InvalidInputException) {
            logger.error("Unable to map given dto '$userDto' to domain object.", e)
            return ApiResult.Failure(
                ErrorCode.MAPPING_ERROR,
                e.getErrorMessage()
            )
        }
        return try {
            if (userRepository.findByUserId(user.userId) != null) {
                logger.info("User with userId '${user.userId}' already exists in database.")
                ApiResult.Failure(
                    ErrorCode.USER_ALREADY_EXIST,
                    "User with userId '${user.userId}' already exists in database."
                )
            } else {
                val persistedUser = userRepository.save(user = user)
                logger.info("Successfully created user '$persistedUser'.")
                ApiResult.Success(persistedUser.userId)
            }

        } catch (e: Exception) {
            logger.error("Unable to create user '$userDto' in database.", e)
            ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
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

    @SuppressWarnings("TooGenericExceptionCaught") // It's intended to catch all exceptions in service
    override fun deleteUser(userId: String?): ApiResult<UUID> {
        logger.info("Start deleting user with userId'$userId'.")
        val userIdResolved = try {
            UUID.fromString(userId)
        } catch (e: IllegalArgumentException) {
            logger.error("Given userId '$userId' is not valid.", e)
            return ApiResult.Failure(ErrorCode.MAPPING_ERROR, "Given userId '$userId' is not valid.")
        }
        val user = try {
            val existingUser = userRepository.findByUserId(userIdResolved)
            if (existingUser == null) {
                logger.error("User with userId '$userId' not found.")
                return ApiResult.Failure(
                    ErrorCode.USER_NOT_FOUND,
                    "User with userId '$userId' does not exist in database."
                )
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

    @SuppressWarnings("TooGenericExceptionCaught") // It's intended to catch all exceptions in service
    override fun updateUser(userDto: UserDto): ApiResult<UUID> {
        logger.info("Start updating user '$userDto'.")
        if (userDto.userId == null || userRepository.findByUserId(userDto.userId) == null) {
            return ApiResult.Failure(
                ErrorCode.USER_NOT_FOUND,
                "User with userId '${userDto.userId}' does not exist in database."
            )
        }
        val user = try {
            userDto.toUser()
        } catch (e: InvalidInputException) {
            logger.error("Unable to map given dto '$userDto' to domain object.", e)
            return ApiResult.Failure(
                ErrorCode.MAPPING_ERROR,
                e.getErrorMessage()
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

    @SuppressWarnings("TooGenericExceptionCaught") // It's intended to catch all exceptions in service
    override fun findUserByUserId(userId: String?): ApiResult<UserOverviewDto> {
        logger.info("Start finding user with userId '$userId'.")
        return try {
            val userIdResolved = try {
                UUID.fromString(userId)
            } catch (e: IllegalArgumentException) {
                logger.error("Given userId '$userId' is not valid.", e)
                return ApiResult.Failure(ErrorCode.MAPPING_ERROR, "Given userId '$userId' is not valid.")
            }
            val user = userRepository.findByUserId(userIdResolved)
            if (user == null) {
                logger.error("User with userId '$userId' not found.")
                ApiResult.Failure(ErrorCode.USER_NOT_FOUND, "User with userId '$userId' does not exist in database.")
            } else {
                logger.info("Successfully found user with userId '$userId'.")
                ApiResult.Success(user.toUserOverviewDto())
            }
        } catch (e: Exception) {
            logger.error("Unable to find user with userId '$userId' from database.'", e)
            ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                e.getErrorMessage()
            )

        }
    }

    private fun User.toUserOverviewDto() = UserOverviewDto(
        userId = this.userId,
        firstName = this.firstName,
        lastName = this.lastName,
        birthdate = this.birthdate.format(DateTimeFormatter.ofPattern(BIRTH_DATE_FORMAT)),
        password = this.password,
        created = this.created.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ofPattern(TIME_STAMP_FORMAT)),
        lastUpdated = this.lastUpdated.truncatedTo(ChronoUnit.SECONDS)
            .format(DateTimeFormatter.ofPattern(TIME_STAMP_FORMAT)),
        account = this.accounts.map { it.toAccountOverviewDto() }
    )

    private fun Account.toAccountOverviewDto() = AccountOverviewDto(
        name = this.name,
        accountId = this.accountId,
        balance = this.balance,
        dispo = this.dispo,
        limit = this.limit,
        created = this.created.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ofPattern(TIME_STAMP_FORMAT)),
        lastUpdated = this.lastUpdated.truncatedTo(ChronoUnit.SECONDS)
            .format(DateTimeFormatter.ofPattern(TIME_STAMP_FORMAT))
    )

    @SuppressWarnings("TooGenericExceptionCaught") // It's intended to catch all exceptions in service
    override fun updatePassword(userPasswordChangeDto: UserPasswordChangeDto): ApiResult<UUID> {
        logger.info("Start updating password for user with userId '${userPasswordChangeDto.userId}'.")
        val user = try {
            val existingUser = userRepository.findByUserId(userPasswordChangeDto.userId)
            if (existingUser == null) {
                logger.error("User with userId '${userPasswordChangeDto.userId}' not found in database.")
                return ApiResult.Failure(
                    ErrorCode.USER_NOT_FOUND,
                    "User with userId '$userPasswordChangeDto' does not exist in database."
                )
            }
            existingUser
        } catch (e: Exception) {
            logger.error("Unable to find user with userId '${userPasswordChangeDto.userId}' from database.'", e)
            return ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                e.getErrorMessage()
            )
        }
        if (userPasswordChangeDto.existingPassword == userPasswordChangeDto.newPassword) {
            logger.error("The given new password cannot be the same as the existing one.")
            return ApiResult.Failure(
                ErrorCode.PASSWORD_ERROR,
                "The new password cannot be the same as the existing one."
            )
        }
        return try {
            val updatedUser = user.copy(
                password = userPasswordChangeDto.newPassword
            )
            val result = userRepository.save(updatedUser)
            logger.info("Successfully updated password for user with userId '${userPasswordChangeDto.userId}'.")
            ApiResult.Success(result.userId)
        } catch (e: IllegalArgumentException) {
            logger.error("Password does not fulfill the requirements.", e)
            ApiResult.Failure(ErrorCode.PASSWORD_ERROR, e.getErrorMessage())
        } catch (e: Exception) {
            logger.error("Unable to update user '$user' to database.", e)
            ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
        }
    }
}

fun Exception.getErrorMessage(): String {
    return message ?: return "Unexpected error occurred."
}