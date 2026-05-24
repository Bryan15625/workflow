package com.bryanhuang.workflow.exception;

public class WorkflowNotFoundException extends RuntimeException {

    public WorkflowNotFoundException(String message) {
        super(message);
    }

    public WorkflowNotFoundException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
