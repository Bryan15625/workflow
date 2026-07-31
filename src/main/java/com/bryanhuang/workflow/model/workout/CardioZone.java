package com.bryanhuang.workflow.model.workout;

import java.util.HashMap;
import java.util.Map;

public enum CardioZone {
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE;

    private static final Map<Integer, CardioZone> cardioZoneMap = new HashMap<>();

    static {
        cardioZoneMap.put(1, CardioZone.ONE);
        cardioZoneMap.put(2, CardioZone.TWO);
        cardioZoneMap.put(3, CardioZone.THREE);
        cardioZoneMap.put(4, CardioZone.FOUR);
        cardioZoneMap.put(5, CardioZone.FIVE);
    }
    public static CardioZone fromInt(Integer zone) {
        if (zone == null || zone < 1 || zone > 5) {
            throw new IllegalArgumentException("Zone cannot be null or outside of [1,5] range");
        }
        return cardioZoneMap.get(zone);
    }

}


