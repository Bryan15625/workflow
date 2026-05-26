package com.bryanhuang.workflow.exception;

import com.bryanhuang.workflow.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation exceptions
     * @param exception
     * @return 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse("Validation failed", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle HTTP message not readable exceptions
     * @param exception
     * @return 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (exception.getCause() instanceof InvalidFormatException invalidFormatException) {
            String fieldPath = invalidFormatException.getPath().stream()
                    .map(reference -> reference.getFieldName() != null
                            ? reference.getFieldName()
                            : "[" + reference.getIndex() + "]")
                    .reduce((first, second) -> first + "." + second)
                    .orElse("requestBody");

            fieldPath = fieldPath.replace(".[", "[");

            errors.put(fieldPath, "Invalid value. Expected " + invalidFormatException.getTargetType().getSimpleName());
        } else {
            errors.put("requestBody", "Malformed JSON or invalid request body");
        }

        ErrorResponse errorResponse = new ErrorResponse("Invalid request body", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(CycleDetectedException.class)
    public ResponseEntity<ErrorResponse> handleCycleDetectedException(CycleDetectedException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid workflow definition",
                Map.of("cycle", exception.getMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidWorkflowDefinitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWorkflowDefinitionException(
            InvalidWorkflowDefinitionException exception
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid workflow definition",
                Map.of("workflowDefinition", exception.getMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle generic exceptions
     * @param exception
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "Internal service error",
                        Map.of("error", exception.getMessage())
                ));
    }
}
