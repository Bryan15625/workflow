package com.bryanhuang.workflow.exception;

public class InvalidRowException extends RuntimeException {

    public InvalidRowException(String message) {
        super(message);
    }

    public InvalidRowException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
