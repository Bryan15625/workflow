package com.bryanhuang.workflow.exception;

import com.bryanhuang.workflow.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleValidationException_returnsBadRequestWithValidationErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError workflowDefinitionNameError = new FieldError(
                "createWorkflowDefinitionRequest",
                "workflowDefinitionName",
                "Workflow definition name is required and cannot be blank"
        );

        FieldError stepNameError = new FieldError(
                "createWorkflowDefinitionRequest",
                "steps[0].stepName",
                "Step name is required and cannot be blank"
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                workflowDefinitionNameError,
                stepNameError
        ));

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
        assertThat(response.getBody().errors())
                .containsEntry(
                        "workflowDefinitionName",
                        "Workflow definition name is required and cannot be blank"
                )
                .containsEntry(
                        "steps[0].stepName",
                        "Step name is required and cannot be blank"
                );
    }

    @Test
    void handleHttpMessageNotReadableException_whenInvalidFormatException_returnsBadRequestWithFieldPath() {
        InvalidFormatException invalidFormatException = InvalidFormatException.from(
                null,
                "Cannot deserialize value",
                "a",
                Integer.class
        );

        invalidFormatException.prependPath(new JsonMappingException.Reference(null, 0));
        invalidFormatException.prependPath(new JsonMappingException.Reference(null, "nextStepIds"));
        invalidFormatException.prependPath(new JsonMappingException.Reference(null, 2));
        invalidFormatException.prependPath(new JsonMappingException.Reference(null, "steps"));

        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Invalid request body",
                invalidFormatException,
                null
        );

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleHttpMessageNotReadableException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid request body");
        assertThat(response.getBody().errors())
                .containsEntry("steps[2].nextStepIds[0]", "Invalid value. Expected Integer");
    }

    @Test
    void handleHttpMessageNotReadableException_whenNotInvalidFormatException_returnsBadRequestWithRequestBodyError() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Malformed JSON"
        );

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleHttpMessageNotReadableException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid request body");
        assertThat(response.getBody().errors())
                .containsEntry("requestBody", "Malformed JSON or invalid request body");
    }

    @Test
    void handleCycleDetectedException_returnsBadRequestWithCycleError() {
        CycleDetectedException exception =
                new CycleDetectedException("Cycle detected in workflow");

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleCycleDetectedException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid workflow");
        assertThat(response.getBody().errors())
                .containsEntry("cycle", "Cycle detected in workflow");
    }

    @Test
    void handleInvalidWorkflowException_returnsBadRequestWithWorkflowError() {
        InvalidWorkflowException exception =
                new InvalidWorkflowException("Workflow must contain at least one terminal step");

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleInvalidWorkflowException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid workflow");
        assertThat(response.getBody().errors())
                .containsEntry("workflow", "Workflow must contain at least one terminal step");
    }

    @Test
    void handleGenericException_returnsInternalServerErrorResponse() {
        Exception exception = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleGenericException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Internal service error");
        assertThat(response.getBody().errors())
                .containsEntry("error", "Something went wrong");
    }
}