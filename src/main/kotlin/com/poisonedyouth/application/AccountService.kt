package com.poisonedyouth.application

import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.notContainsAccount
import com.poisonedyouth.persistence.AccountRepository
import com.poisonedyouth.persistence.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter
import java.util.*

interface AccountService {
    fun createAccount(userId: String?, accountDto: AccountDto): ApiResult<UUID>
    fun updateAccount(userId: String?, accountDto: AccountDto): ApiResult<UUID>
    fun deleteAccount(userId: String?, accountId: String?): ApiResult<UUID>
    fun findByUserIdAndAccountId(userId: String?, accountId: String?): ApiResult<AccountOverviewDto>
}

class AccountServiceImpl(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
) : AccountService {
    private val logger: Logger = LoggerFactory.getLogger(AccountService::class.java)

    @SuppressWarnings("TooGenericExceptionCaught") // It's intended to catch all exceptions in service
    override fun createAccount(userId: String?, accountDto: AccountDto): ApiResult<UUID> {
        logger.info("Start creation of account '$accountDto' for user with userId '$userId'.")
        return try {
            val userIdResolved = UUID.fromString(userId)
            val user = userRepository.findByUserId(userIdResolved)
            if (user == null) {
                logger.error("User with userId '$userId' does not exist in database.")
                return ApiResult.Failure(
                    ErrorCode.USER_NOT_FOUND,
                    "User with userId '$userId' does not exist in database."
                )
            }
            val account = accountDto.toAccount()
            if (accountRepository.findByAccountId(account.accountId) != null) {
                logger.info("Account with accountId '${account.accountId}' already exist in database.")
                ApiResult.Failure(
                    ErrorCode.ACCOUNT_ALREADY_EXIST,
                    "Account with accountId '${account.accountId}' already exist in database."
                )
            } else {
                val persistedAccount = accountRepository.saveForUser(user = user, account = account)
                logger.info("Successfully created account '$persistedAccount' for user with userId '$userId'.")
                ApiResult.Success(persistedAccount.accountId)
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Given userId '$userId' is not valid.", e)
            ApiResult.Failure(ErrorCode.MAPPING_ERROR, "Given userId '$userId' is not valid.")
        } catch (e: InvalidInputException) {
            logger.error("Unable to map given dto '$accountDto' to domain object.", e)
            ApiResult.Failure(
                ErrorCode.MAPPING_ERROR,
                e.getErrorMessage()
            )
        } catch (e: Exception) {
            logger.error("Unable to create account '$accountDto' in database.", e)
            ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
        }
    }

    private fun AccountDto.toAccount() = try {
        val account = Account(
            name = this.name,
            dispo = this.dispo,
            limit = this.limit
        )
        if (this.accountId != null) {
            account.copy(
                accountId = this.accountId
            )
        } else {
            account
        }
    } catch (e: IllegalArgumentException) {
        throw InvalidInputException("Given AccountDto '$this' is not valid.", e)
    }

    @SuppressWarnings("TooGenericExceptionCaught") // It's intended to catch all exceptions in service
    override fun updateAccount(userId: String?, accountDto: AccountDto): ApiResult<UUID> {
        logger.info("Start update of account '$accountDto' for user with userId '$userId'.")
        return try {
            val userIdResolved = UUID.fromString(userId)
            val user = userRepository.findByUserId(userIdResolved)
            if (user == null) {
                logger.error("User with userId '$userId' does not exist in database.")
                return ApiResult.Failure(
                    ErrorCode.USER_NOT_FOUND,
                    "User with userId '$userId' does not exist in database."
                )
            }
            val existingAccount = accountDto.accountId?.let { accountRepository.findByAccountId(it) }
                ?: return ApiResult.Failure(
                    ErrorCode.ACCOUNT_NOT_FOUND,
                    "Account with accountId '${accountDto.accountId}' does not exist in database."
                )
            val account = accountDto.toAccount().copy(
                balance = existingAccount.balance
            )
            if (user.accounts.notContainsAccount(account)) {
                logger.error("Account with accountId '${account.accountId}' does not belong to user with userId '$userId'")
                return ApiResult.Failure(
                    ErrorCode.NOT_ALLOWED,
                    "Account with accountId '${account.accountId}' does not belong to user with userId '$userId'"
                )
            }
            val updatedAccount = accountRepository.updateForUser(user = user, account = account)
            logger.info("Successfully updated account '$updatedAccount'.")
            ApiResult.Success(updatedAccount.accountId)
        } catch (e: IllegalArgumentException) {
            logger.error("Given userId '$userId' is not valid.", e)
            return ApiResult.Failure(ErrorCode.MAPPING_ERROR, "Given userId '$userId' is not valid.")
        } catch (e: InvalidInputException) {
            logger.error("Unable to map given dto '$accountDto' to domain object.", e)
            ApiResult.Failure(
                ErrorCode.MAPPING_ERROR,
                e.getErrorMessage()
            )
        } catch (e: Exception) {
            logger.error("Unable to update account '$accountDto' to database.", e)
            ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
        }
    }

    @SuppressWarnings("TooGenericExceptionCaught") // It's intended to catch all exceptions in service
    override fun deleteAccount(userId: String?, accountId: String?): ApiResult<UUID> {
        logger.info("Start deleting of account with accountId '$accountId' for user with userId '${userId}'.")
        return try {
            val userIdResolved = UUID.fromString(userId)
            val accountIdResolved = UUID.fromString(accountId)

            val user = userRepository.findByUserId(userIdResolved)
            if (user == null) {
                logger.error("User with userId '$userId' does not exist in database.")
                return ApiResult.Failure(
                    ErrorCode.USER_NOT_FOUND,
                    "User with userId '$userId' does not exist in database."
                )
            }
            val existingAccount = accountRepository.findByAccountId(accountIdResolved)
                ?: return ApiResult.Failure(
                    ErrorCode.ACCOUNT_NOT_FOUND,
                    "Account with accountId '$accountId' does not exist in database."
                )
            if (user.accounts.notContainsAccount(existingAccount)) {
                return ApiResult.Failure(
                    ErrorCode.NOT_ALLOWED,
                    "Account with accountId '${accountId}' does not belong to user with userId '$userId'"
                )
            }
            accountRepository.delete(existingAccount)
            logger.info("Successfully deleted account with accountId '$accountId'.")
            return ApiResult.Success(accountIdResolved)
        } catch (e: IllegalArgumentException) {
            logger.error("Given userId '$userId' is not valid.", e)
            return ApiResult.Failure(ErrorCode.MAPPING_ERROR, "Given userId '$userId' is not valid.")
        } catch (e: Exception) {
            logger.error("Account with accountId '${accountId}' cannot be deleted.", e)
            ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                "Account with accountId '${accountId}' cannot be deleted."
            )
        }
    }

    override fun findByUserIdAndAccountId(userId: String?, accountId: String?): ApiResult<AccountOverviewDto> {
        logger.info("Start finding of account with accountId '$accountId' for user with userId '${userId}'.")
        return try {
            val userIdResolved = UUID.fromString(userId)
            val user = userRepository.findByUserId(userIdResolved)
            if (user == null) {
                logger.error("User with userId '$userId' does not exist in database.")
                return ApiResult.Failure(
                    ErrorCode.USER_NOT_FOUND,
                    "User with userId '$userId' does not exist in database."
                )
            }
            val accountIdIdResolved = UUID.fromString(accountId)
            val existingAccount = accountRepository.findByAccountId(accountIdIdResolved)
                ?: return ApiResult.Failure(
                    ErrorCode.ACCOUNT_NOT_FOUND,
                    "Account with accountId '$accountId' does not exist in database."
                )
            if (user.accounts.notContainsAccount(existingAccount)) {
                return ApiResult.Failure(
                    ErrorCode.NOT_ALLOWED,
                    "Account with accountId '${accountId}' does not belong to user with userId '$userId'"
                )
            }
            logger.info("Successfully found account with accountId '$accountId'.")
            ApiResult.Success(
                AccountOverviewDto(
                    name = existingAccount.name,
                    accountId = existingAccount.accountId,
                    balance = existingAccount.balance,
                    dispo = existingAccount.dispo,
                    limit = existingAccount.limit,
                    created = existingAccount.created.format(DateTimeFormatter.ofPattern(TIME_STAMP_FORMAT)),
                    lastUpdated = existingAccount.lastUpdated.format(DateTimeFormatter.ofPattern(TIME_STAMP_FORMAT))
                )
            )
        } catch (e: IllegalArgumentException) {
            logger.error("Given userId '$userId' or '$accountId' is not valid.", e)
            ApiResult.Failure(ErrorCode.MAPPING_ERROR, "Given userId '$userId' or '$accountId' is not valid.")
        } catch (e: Exception) {
            logger.error("Account with accountId '${accountId}' cannot be deleted.", e)
            ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                "Account with accountId '${accountId}' cannot be deleted."
            )
        }
    }
}
