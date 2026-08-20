package com.bryanhuang.workflow.exception;

public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(String message) {
        super(message);
    }

    public ReportNotFoundException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
