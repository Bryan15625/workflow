package com.bryanhuang.workflow.model.workout;

public enum WorkoutRir {
    NONE,
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN;

    private static final WorkoutRir[] VALUES = WorkoutRir.values();

    public static WorkoutRir fromString(String rirStr) {
        // Handle the empty case (,,)
        if (rirStr == null || rirStr.trim().isEmpty()) {
            return WorkoutRir.NONE;
        }

        // Handle numeric cases (,0,, ,1,, etc.)
        try {
            int rirValue = Integer.parseInt(rirStr.trim());
            return fromInt(rirValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid RIR value: '" + rirStr + "'", e);
        }
    }

    /**
     * Map numeric primitives directly to the Enum positions.
     */
    public static WorkoutRir fromInt(int rir) {
        // Shift by 1 because NONE is at index 0, ZERO is at index 1...
        int targetIndex = rir + 1;

        // Validation boundary check (allows 0 through 10)
        if (targetIndex < 1 || targetIndex >= VALUES.length) {
            throw new IllegalArgumentException("RIR integer value must be between 0 and 10. Found: " + rir);
        }

        return VALUES[targetIndex];
    }
}