package com.example.kontaktiotask.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(GroupServiceException.class)
    public ResponseEntity<ErrorResponse> handleGroupServiceException(GroupServiceException ex) {
        log.error("Group service exception: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getReason(),
                String.valueOf(ex.getStatusCode().value())
        );
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    @ExceptionHandler(AssetServiceException.class)
    public ResponseEntity<ErrorResponse> handleAssetException(AssetServiceException ex) {
        log.error("Asset service exception: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getReason(),
                String.valueOf(ex.getStatusCode().value())
        );
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleCustomValidationException(ValidationException ex) {
        log.error("Validation exception: ", ex);
        String errorMessage = "Validation error";

        if (ex instanceof ConstraintViolationException constraintEx) {
            StringBuilder messages = new StringBuilder();
            for (ConstraintViolation<?> violation : constraintEx.getConstraintViolations()) {
                messages.append(violation.getMessageTemplate()).append(" ");
            }
            errorMessage = messages.toString().trim();
        }

        ErrorResponse errorResponse = new ErrorResponse(
                errorMessage,
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
