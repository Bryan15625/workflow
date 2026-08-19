package com.bryanhuang.workflow.model.workout;

public record Range(double min, double max) {
    public boolean contains(double value) {
        return value >= min && value <= max;
    }
}
