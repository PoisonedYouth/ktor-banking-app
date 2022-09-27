package com.poisonedyouth.application

import com.poisonedyouth.domain.Administrator
import com.poisonedyouth.persistence.AdministratorRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


interface AdministratorService {
    fun isValidAdministrator(administratorId: String?, password: String): ApiResult<Boolean>
}

class AdministratorServiceImpl(
    private val administratorRepository: AdministratorRepository
) : AdministratorService {
    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)

    @SuppressWarnings("TooGenericExceptionCaught") // It's intended to catch all exceptions in service
    override fun isValidAdministrator(administratorId: String?, password: String): ApiResult<Boolean> {
        logger.info("Start checking for valid administrator with administrator '${administratorId}' and password '$password'.")
        return try {
            val existingAdministrator = findAdministratorByAdministratorId(administratorId)
            if (existingAdministrator == null) {
                logger.error("Administrator with administratorId '$administratorId' does not exist in database.")
                return ApiResult.Failure(
                    ErrorCode.ADMINISTRATOR_NOT_FOUND,
                    "Administrator with administratorId '$administratorId' does not exist in database."
                )
            }

            if (existingAdministrator.password != password) {
                logger.error("Password for administrator with administratorId '${administratorId}' is not valid.")
                return ApiResult.Failure(
                    ErrorCode.NOT_ALLOWED,
                    "Password for administrator with administratorId '${administratorId}' is not valid."
                )
            }

            logger.info("Successfully check for valid administrator with administratorId '${administratorId}'.")
            ApiResult.Success(true)
        } catch (e: IllegalArgumentException) {
            logger.error("Given administratorId '$administratorId' is not valid.", e)
            ApiResult.Failure(ErrorCode.MAPPING_ERROR, "Given administrator '$administratorId' is not valid.")
        } catch (e: Exception) {
            logger.error(
                "Unable check for valid administrator with administratorId '${administratorId}' in database.",
                e
            )
            ApiResult.Failure(ErrorCode.DATABASE_ERROR, e.getErrorMessage())
        }
    }

    private fun findAdministratorByAdministratorId(administratorId: String?): Administrator? {
        val administratorIdResolved = UUID.fromString(administratorId)
        return administratorRepository.findByAdministratorId(administratorIdResolved)
    }

}