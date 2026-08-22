package com.bryanhuang.workflow.exception;

public class PdfGenerationException extends RuntimeException {

    public PdfGenerationException(String message) {
        super(message);
    }

    public PdfGenerationException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
