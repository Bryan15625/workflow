package com.bryanhuang.workflow.exception;

public class WorkoutIdealNotFoundException extends RuntimeException {

  public WorkoutIdealNotFoundException(String message) {
    super(message);
  }

  public WorkoutIdealNotFoundException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }
}
