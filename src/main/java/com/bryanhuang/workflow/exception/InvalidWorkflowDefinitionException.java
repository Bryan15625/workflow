package com.bryanhuang.workflow.exception;

public class InvalidWorkflowDefinitionException extends RuntimeException {

    public InvalidWorkflowDefinitionException(String message) {
        super(message);
    }

    public InvalidWorkflowDefinitionException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
