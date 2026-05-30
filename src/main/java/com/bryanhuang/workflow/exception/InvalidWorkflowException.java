package com.bryanhuang.workflow.exception;

public class InvalidWorkflowException extends RuntimeException {

    public InvalidWorkflowException(String message) {
        super(message);
    }

    public InvalidWorkflowException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
