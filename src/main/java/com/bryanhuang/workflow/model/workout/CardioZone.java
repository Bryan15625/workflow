package com.bryanhuang.workflow.model.workout;

public enum CardioZone {
    NONE,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE;

    private static final CardioZone[] VALUES = CardioZone.values();

    public static CardioZone fromInt(int zone) {

        if (zone < 1 || zone >= VALUES.length) {
            throw new IllegalArgumentException(
                    "Cardio zone integer must be between 1 and 5. Found: " + zone
            );
        }

        return VALUES[zone];
    }
}