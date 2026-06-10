package com.bryanhuang.workflow.exception;

public class WorkflowExecutionNotFoundException extends RuntimeException {

    public WorkflowExecutionNotFoundException(String message) {
        super(message);
    }

    public WorkflowExecutionNotFoundException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
