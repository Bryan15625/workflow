package com.bryanhuang.workflow.exception;

public class WorkflowExecutionException extends RuntimeException {

    public WorkflowExecutionException(String message) {
        super(message);
    }

    public WorkflowExecutionException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
