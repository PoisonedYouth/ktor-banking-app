package com.poisonedyouth.application

import com.poisonedyouth.domain.Account
import com.poisonedyouth.persistence.AccountRepository
import com.poisonedyouth.persistence.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

interface AccountService {

    fun createAccount(userId: UUID, accountDto: AccountDto): ApiResult<UUID>

    fun updateAccount(userId: UUID, accountDto: AccountDto): ApiResult<UUID>
    fun deleteAccount(userId: UUID, accountId: UUID): ApiResult<UUID>
}

class AccountServiceImpl(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
) : AccountService {
    private val logger: Logger = LoggerFactory.getLogger(AccountService::class.java)

    override fun createAccount(userId: UUID, accountDto: AccountDto): ApiResult<UUID> {
        logger.info("Start creation of account '$accountDto' for user with userId '$userId'.")
        val user = userRepository.findByUserId(userId)
        if (user == null) {
            logger.error("User with userId '$userId' not found.")
            return ApiResult.Failure(ErrorCode.USER_NOT_FOUND, "User with userId '$userId' not found.")
        }
        val account = try {
            accountDto.toAccount()
        } catch (e: InvalidInputException) {
            logger.error("Unable to map given dto '$accountDto' to domain object.", e)
            return ApiResult.Failure(
                ErrorCode.MAPPING_ERROR,
                e.getErrorMessage()
            )
        }
        return try {
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

    override fun updateAccount(userId: UUID, accountDto: AccountDto): ApiResult<UUID> {
        logger.info("Start update of account '$accountDto' for user with userId '$userId'.")
        val user = userRepository.findByUserId(userId)
        if (user == null) {
            logger.error("User with userId '$userId' not found.")
            return ApiResult.Failure(ErrorCode.USER_NOT_FOUND, "User with userId '$userId' not found.")
        }
        if (accountDto.accountId == null || accountRepository.findByAccountId(accountDto.accountId) == null) {
            return ApiResult.Failure(
                ErrorCode.ACCOUNT_NOT_FOUND,
                "Account with accountId '${accountDto.accountId}' does not exist in database."
            )
        }
        val account = try {
            accountDto.toAccount()
        } catch (e: InvalidInputException) {
            logger.error("Unable to map given dto '$accountDto' to domain object.", e)
            return ApiResult.Failure(
                ErrorCode.MAPPING_ERROR,
                e.getErrorMessage()
            )
        }
        if (user.accounts.find { it.accountId == account.accountId } === null) {
            return ApiResult.Failure(
                ErrorCode.NOT_ALLOWED,
                "Account with accountId '${account.accountId}' does not belong to user with userId '$userId'"
            )
        }
        return try {
            val updatedAccount = accountRepository.updateForUser(user = user, account = account)
            logger.info("Successfully updated account '$updatedAccount'.")
            ApiResult.Success(updatedAccount.accountId)
        } catch (e: Exception) {
            logger.error("Unable to update account '$accountDto' to database.", e)
            ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
        }
    }

    override fun deleteAccount(userId: UUID, accountId: UUID): ApiResult<UUID> {
        logger.info("Start deleting of account with accountId '$accountId' for user with userId '${userId}'.")
        try {
            val user = userRepository.findByUserId(userId)
            if (user == null) {
                logger.error("User with userId '$userId' not found.")
                return ApiResult.Failure(ErrorCode.USER_NOT_FOUND, "User with userId '$userId' not found.")
            }
            val existingAccount = accountRepository.findByAccountId(accountId)
                ?: return ApiResult.Failure(
                    ErrorCode.ACCOUNT_NOT_FOUND,
                    "Account with accountId '$accountId' is not available in database."
                )
            if (user.accounts.find { it.accountId == existingAccount.accountId } === null) {
                return ApiResult.Failure(
                    ErrorCode.NOT_ALLOWED,
                    "Account with accountId '${accountId}' does not belong to user with userId '$userId'"
                )
            }
            accountRepository.delete(existingAccount)
            logger.info("Successfully deleted account with accountId '$accountId'.")
            return ApiResult.Success(accountId)
        } catch (e: Exception) {
            return ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                "Account with accountId '${accountId}' cannot be deleted."
            )
        }
    }
}
