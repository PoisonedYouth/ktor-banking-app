package com.poisonedyouth.application

import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.Transaction
import com.poisonedyouth.domain.notContainsAccount
import com.poisonedyouth.persistence.AccountRepository
import com.poisonedyouth.persistence.TransactionRepository
import com.poisonedyouth.persistence.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

interface TransactionService {
    fun createTransaction(userId: String?, transactionDto: TransactionDto): ApiResult<UUID>
    fun deleteTransaction(transactionId: String?): ApiResult<UUID>
}

class TransactionServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository
) : TransactionService {
    private val logger: Logger = LoggerFactory.getLogger(TransactionService::class.java)

    @SuppressWarnings("TooGenericExceptionCaught") // It's intended to catch all exceptions in service
    override fun createTransaction(userId: String?, transactionDto: TransactionDto): ApiResult<UUID> {
        logger.info("Start creation of transaction '${transactionDto}.")
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
                return ApiResult.Failure(ErrorCode.USER_NOT_FOUND, "User with userId '$userId' not found.")
            }
            existingUser
        } catch (e: Exception) {
            logger.error("Unable to find user with userId '$userId' in database.", e)
            return ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
        }
        val origin = try {
            accountRepository.findByAccountId(transactionDto.origin)
                ?: return ApiResult.Failure(
                    ErrorCode.ACCOUNT_NOT_FOUND,
                    "Account with accountId '${transactionDto.origin}' does not exist in database."
                )
        } catch (e: Exception) {
            logger.error("Unable to find account with accountId '${transactionDto.origin}' in database.", e)
            return ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
        }
        if (user.accounts.notContainsAccount(origin)) {
            return ApiResult.Failure(
                ErrorCode.NOT_ALLOWED,
                "Account with accountId '${origin.accountId}' does not belong to user with userId '$userId'"
            )
        }

        val target = try {
            accountRepository.findByAccountId(transactionDto.target)
                ?: return ApiResult.Failure(
                    ErrorCode.ACCOUNT_NOT_FOUND,
                    "Account with accountId '${transactionDto.target}' does not exist in database."
                )
        } catch (e: Exception) {
            logger.error("Unable to find account with accountId '${transactionDto.target}' in database.", e)
            return ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
        }

        if (origin.balance + origin.dispo < transactionDto.amount) {
            logger.error("Origin account with accountId '${origin.accountId}' has not enough balance for transaction.")
            return ApiResult.Failure(ErrorCode.TRANSACTION_REQUEST_INVALID, "Not enough balance for transaction.")
        }
        if (origin.limit < transactionDto.amount) {
            logger.error("Origin account with accountId '${origin.accountId}' has not enough balance for transaction.")
            return ApiResult.Failure(ErrorCode.TRANSACTION_REQUEST_INVALID, "Not enough balance for transaction.")
        }

        val transaction = try {
            createTransaction(transactionDto = transactionDto, origin = origin, target = target)
        } catch (e: InvalidInputException) {
            logger.error("Unable to map given dto '$transactionDto' to domain object.", e)
            return ApiResult.Failure(
                ErrorCode.MAPPING_ERROR,
                e.getErrorMessage()
            )
        }

        return try {
            val persistedTransaction = transactionRepository.save(transaction)
            logger.info("Successfully created transaction '$persistedTransaction'.")
            ApiResult.Success(persistedTransaction.transactionId)
        } catch (e: Exception) {
            logger.error("Unable to create transaction '$transactionDto' in database.", e)
            ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
        }
    }

    private fun createTransaction(transactionDto: TransactionDto, origin: Account, target: Account) = try {
        val transaction = Transaction(
            origin = origin,
            target = target,
            amount = transactionDto.amount
        )
        if (transactionDto.transactionId != null) {
            transaction.copy(
                transactionId = transactionDto.transactionId
            )
        } else {
            transaction
        }
    } catch (e: IllegalArgumentException) {
        throw InvalidInputException("Given TransactionDto '$transactionDto' is not valid.", e)
    }

    @SuppressWarnings("TooGenericExceptionCaught") // It's intended to catch all exceptions in service
    override fun deleteTransaction(transactionId: String?): ApiResult<UUID> {
        logger.info("Start delete of transaction with transactionId '$transactionId'.")
        val transactionIdResolved = try {
            UUID.fromString(transactionId)
        } catch (e: IllegalArgumentException) {
            logger.error("Given transactionId '$transactionId' is not valid.", e)
            return ApiResult.Failure(ErrorCode.MAPPING_ERROR, "Given transactionId '$transactionId' is not valid.")
        }
        val transaction = try {
            val existingTransaction = transactionRepository.findByTransactionId(transactionIdResolved)
            if (existingTransaction == null) {
                logger.error("Transaction with transactionId '$transactionId' cannot be found.")
                return ApiResult.Failure(
                    ErrorCode.TRANSACTION_NOT_FOUND,
                    "Transaction with transactionId '$transactionId' cannot be found."
                )
            }
            existingTransaction
        } catch (e: Exception) {
            logger.error("Cannot find transaction with transactionId '$transactionId'.", e)
            return ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                "Cannot find transaction with transactionId '$transactionId'."
            )
        }
        try {
            val origin = transaction.origin.copy(
                balance = transaction.origin.balance + transaction.amount
            )
            accountRepository.updateAccount(origin)
        } catch (e: Exception) {
            logger.error("Account with accountId '${transaction.origin.accountId}' cannot be updated to database.", e)
            return ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                "Account with accountId '${transaction.origin.accountId}' cannot be updated to database."
            )
        }
        try {
            val target = transaction.target.copy(
                balance = transaction.origin.balance - transaction.amount
            )
            accountRepository.updateAccount(target)
        } catch (e: Exception) {
            logger.error("Account with accountId '${transaction.origin.accountId}' cannot be updated to database.", e)
            return ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                "Account with accountId '${transaction.origin.accountId}' cannot be updated to database."
            )
        }
        logger.info("Successfully deleted transaction with transactionId '$transactionId'.")
        return ApiResult.Success(transactionIdResolved)
    }
}