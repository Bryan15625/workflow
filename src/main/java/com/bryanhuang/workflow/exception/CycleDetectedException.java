package com.bryanhuang.workflow.exception;

public class CycleDetectedException extends RuntimeException {

    public CycleDetectedException(String message) {
        super(message);
    }

    public CycleDetectedException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
