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
    fun getTransaction(userId: String?, transactionId: String?): ApiResult<TransactionDto>
    fun deleteTransaction(transactionId: String?): ApiResult<UUID>
    fun getAllTransactions(): ApiResult<List<TransactionDto>>
    fun getAllTransactionByAccount(accountId: String?): ApiResult<List<TransactionDto>>
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
        return try {
            val userIdResolved = UUID.fromString(userId)
            val existingUser = userRepository.findByUserId(userIdResolved)
            if (existingUser == null) {
                logger.error("User with userId '$userId' does not exist in database.")
                return ApiResult.Failure(
                    ErrorCode.USER_NOT_FOUND,
                    "User with userId '$userId' does not exist in database."
                )
            }
            val origin = accountRepository.findByAccountId(transactionDto.origin)
                ?: return ApiResult.Failure(
                    ErrorCode.ACCOUNT_NOT_FOUND,
                    "Account with accountId '${transactionDto.origin}' does not exist in database."
                )
            if (existingUser.accounts.notContainsAccount(origin)) {
                return ApiResult.Failure(
                    ErrorCode.NOT_ALLOWED,
                    "Account with accountId '${origin.accountId}' does not belong to user with userId '$userId'"
                )
            }
            val target = accountRepository.findByAccountId(transactionDto.target)
                ?: return ApiResult.Failure(
                    ErrorCode.ACCOUNT_NOT_FOUND,
                    "Account with accountId '${transactionDto.target}' does not exist in database."
                )

            if (origin.balance + origin.dispo < transactionDto.amount) {
                logger.error("Origin account with accountId '${origin.accountId}' has not enough balance for transaction.")
                return ApiResult.Failure(ErrorCode.TRANSACTION_REQUEST_INVALID, "Not enough balance for transaction.")
            }
            if (origin.limit < transactionDto.amount) {
                logger.error("Origin account with accountId '${origin.accountId}' has not enough limit for transaction.")
                return ApiResult.Failure(ErrorCode.TRANSACTION_REQUEST_INVALID, "Not enough balance for transaction.")
            }

            val transaction = createTransaction(transactionDto = transactionDto, origin = origin, target = target)

            val persistedTransaction = transactionRepository.save(transaction)
            logger.info("Successfully created transaction '$persistedTransaction'.")
            ApiResult.Success(persistedTransaction.transactionId)
        } catch (e: IllegalArgumentException) {
            logger.error("Given userId '$userId' is not valid.", e)
            return ApiResult.Failure(ErrorCode.MAPPING_ERROR, "Given userId '$userId' is not valid.")
        } catch (e: InvalidInputException) {
            logger.error("Unable to map given dto '$transactionDto' to domain object.", e)
            ApiResult.Failure(
                ErrorCode.MAPPING_ERROR,
                e.getErrorMessage()
            )
        } catch (e: Exception) {
            logger.error("Unable to create transaction '$transactionDto' in database.", e)
            ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                "Unable to create transaction '$transactionDto' in database."
            )
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
        return try {
            val transactionIdResolved = UUID.fromString(transactionId)

            val existingTransaction = transactionRepository.findByTransactionId(transactionIdResolved)
            if (existingTransaction == null) {
                logger.error("Transaction with transactionId '$transactionId' does not exist in database.")
                return ApiResult.Failure(
                    ErrorCode.TRANSACTION_NOT_FOUND,
                    "Transaction with transactionId '$transactionId' does not exist in database."
                )
            }
            val origin = existingTransaction.origin.copy(
                balance = existingTransaction.origin.balance + existingTransaction.amount
            )
            accountRepository.updateAccount(origin)

            val target = existingTransaction.target.copy(
                balance = existingTransaction.target.balance - existingTransaction.amount
            )
            accountRepository.updateAccount(target)
            transactionRepository.delete(existingTransaction)
            logger.info("Successfully deleted transaction with transactionId '$transactionId'.")
            ApiResult.Success(existingTransaction.transactionId)
        } catch (e: IllegalArgumentException) {
            logger.error("Given transactionId '$transactionId' is not valid.", e)
            return ApiResult.Failure(ErrorCode.MAPPING_ERROR, "Given transactionId '$transactionId' is not valid.")
        } catch (e: Exception) {
            logger.error("Cannot delete transaction with transactionId '$transactionId' from database.", e)
            ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                "Cannot delete transaction with transactionId '$transactionId' from database."
            )

        }
    }

    override fun getTransaction(userId: String?, transactionId: String?): ApiResult<TransactionDto> {
        logger.info("Start finding of transaction with transactionId '$transactionId'.")
        return try {
            val userIdResolved = UUID.fromString(userId)
            val existingUser = userRepository.findByUserId(userIdResolved)
            if (existingUser == null) {
                logger.error("User with userId '$userId' does not exist in database.")
                return ApiResult.Failure(
                    ErrorCode.USER_NOT_FOUND,
                    "User with userId '$userId' does not exist in database."
                )
            }
            val transactionIdResolved = UUID.fromString(transactionId)

            val existingTransaction = transactionRepository.findByTransactionId(transactionIdResolved)
            if (existingTransaction == null) {
                logger.error("Transaction with transactionId '$transactionId' cannot be found.")
                return ApiResult.Failure(
                    ErrorCode.TRANSACTION_NOT_FOUND,
                    "Transaction with transactionId '$transactionId' cannot be found."
                )
            }

            if (existingUser.accounts.notContainsAccount(existingTransaction.origin, existingTransaction.target)) {
                logger.error("Transaction with transactionId '$transactionId' does not belong to user with userId '$userId'.")
                return ApiResult.Failure(
                    ErrorCode.NOT_ALLOWED,
                    "Transaction with transactionId '$transactionId' does not belong to user with userId '$userId'."
                )
            }

            logger.info("Successfully find transaction with transactionId '$transactionId'.")
            ApiResult.Success(existingTransaction.toTransactionDto())
        } catch (e: IllegalArgumentException) {
            logger.error("Given transactionId '$transactionId' or userId '$userId' is not valid.", e)
            return ApiResult.Failure(
                ErrorCode.MAPPING_ERROR,
                "Given transactionId '$transactionId' or userId '$userId' is not valid."
            )
        } catch (e: Exception) {
            logger.error("Cannot delete transaction with transactionId '$transactionId' from database.", e)
            ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                "Cannot find transaction with transactionId '$transactionId' in database."
            )

        }
    }

    override fun getAllTransactions(): ApiResult<List<TransactionDto>> {
        logger.info("Start loading all transactions.")
        return try {
            ApiResult.Success(transactionRepository.findAll().map { it.toTransactionDto() })
        } catch (e: Exception) {
            logger.error("Cannot load transactions from database.", e)
            ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                "Cannot load transactions from database."
            )
        }
    }

    override fun getAllTransactionByAccount(accountId: String?): ApiResult<List<TransactionDto>> {
        logger.info("Start loading transactions for account with accountId '$accountId'.")
        return try {
            val accountIdResolved = UUID.fromString(accountId)
            val existingAccount = accountRepository.findByAccountId(accountIdResolved)
            if (existingAccount == null) {
                logger.error("Account with accountId '$accountId' does not exist in database.")
                return ApiResult.Failure(
                    ErrorCode.ACCOUNT_NOT_FOUND,
                    "User with userId '$accountId' does not exist in database."
                )
            }
            return ApiResult.Success(transactionRepository.findAllByAccount(existingAccount).map {
                it.toTransactionDto()
            })
        } catch (e: IllegalArgumentException) {
            logger.error("Given accountId '$accountId'Id is not valid.", e)
            return ApiResult.Failure(
                ErrorCode.MAPPING_ERROR,
                "Given accountId '$accountId'Id is not valid."
            )
        } catch (e: Exception) {
            logger.error("Cannot load transactions from database.", e)
            ApiResult.Failure(
                ErrorCode.DATABASE_ERROR,
                "Cannot load transactions from database."
            )

        }
    }

    private fun Transaction.toTransactionDto() = TransactionDto(
        transactionId = this.transactionId,
        origin = this.origin.accountId,
        target = this.target.accountId,
        amount = this.amount
    )
}