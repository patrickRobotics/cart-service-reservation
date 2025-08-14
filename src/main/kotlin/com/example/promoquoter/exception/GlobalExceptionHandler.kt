package com.example.promoquoter.exception

import com.example.promoquoter.controller.CartController
import com.example.promoquoter.service.InsufficientStockException
import com.example.promoquoter.service.ProductNotFoundException
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.LocalDateTime


@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldErrors = mutableMapOf<String, String>()
        val globalErrors = mutableListOf<String>()

        ex.bindingResult.allErrors.forEach { error ->
            when (error) {
                is FieldError -> {
                    val fieldName = error.field
                    val errorMessage = error.defaultMessage ?: "Invalid value"
                    fieldErrors[fieldName] = errorMessage
                }
                is ObjectError -> {
                    globalErrors.add(error.defaultMessage ?: "Validation error")
                }
            }
        }

        val details = mutableMapOf<String, Any>()
        if (fieldErrors.isNotEmpty()) details["fieldErrors"] = fieldErrors
        if (globalErrors.isNotEmpty()) details["globalErrors"] = globalErrors

        logger.error(ex.message, details)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Invalid input parameters",
            details = details
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleHandlerMethodValidationException(ex: HandlerMethodValidationException): ResponseEntity<ErrorResponse> {
        val fieldErrors = mutableMapOf<String, String>()

        ex.allErrors.forEach { error ->
            val fieldName = when (error) {
                is FieldError -> error.field
                is ObjectError -> error.objectName
                else -> "unknown field"
            }
            val errorMessage = error.defaultMessage ?: "Invalid value"
            fieldErrors[fieldName] = errorMessage
        }

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Invalid method parameters",
            details = mapOf("fieldErrors" to fieldErrors)
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        logger.error(ex.message, ex.cause)

        val message = when (val cause = ex.cause) {
            is JsonParseException -> "Invalid JSON format: ${cause.originalMessage}"
            is InvalidFormatException -> {
                val fieldPath = cause.path.joinToString(".") { it.fieldName ?: "[${it.index}]" }
                "Invalid value for field '$fieldPath': expected ${cause.targetType.simpleName}"
            }
            is MismatchedInputException -> {
                val fieldPath = cause.path.joinToString(".") { it.fieldName ?: "[${it.index}]" }
                when {
                    cause.message?.contains("missing") == true -> "Missing required field: '$fieldPath'"
                    else -> "Invalid input format for field '$fieldPath'"
                }
            }
            else -> "Invalid request format"
        }

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid Request Format",
            message = message
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val message = "Invalid value for parameter '${ex.name}': expected ${ex.requiredType?.simpleName}"
        logger.error(message)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Parameter Type Mismatch",
            message = message
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFoundException(ex: ProductNotFoundException): ResponseEntity<ErrorResponse> {
        logger.error(ex.message)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Product Not Found",
            message = ex.message ?: "Product not found"
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(InsufficientStockException::class)
    fun handleInsufficientStockException(ex: InsufficientStockException): ResponseEntity<ErrorResponse> {
        logger.error(ex.message)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Insufficient Stock",
            message = ex.message ?: "Insufficient stock available"
        )

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        logger.error(ex.message)
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Data Integrity Violation",
            message = "A constraint violation occurred. This may be due to duplicate idempotency key."
        )

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.error(ex.message)
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid Argument",
            message = ex.message ?: "Invalid argument provided"
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error(ex.message, ex)
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred"
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val details: Map<String, Any>? = null
)