package com.bryanhuang.workflow.exception;

public class StepExecutionStatusNotFoundException extends RuntimeException {

    public StepExecutionStatusNotFoundException(String message) {
        super(message);
    }

    public StepExecutionStatusNotFoundException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}