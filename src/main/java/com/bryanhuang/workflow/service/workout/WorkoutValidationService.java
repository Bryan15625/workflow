package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.exception.InvalidRowException;
import com.bryanhuang.workflow.model.workout.CardioZone;
import com.bryanhuang.workflow.model.workout.WorkoutRir;
import com.bryanhuang.workflow.model.workout.WorkoutType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutValidationService {

    public String parseUserId(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidRowException("Missing value for " + field + " field");
        }
        return raw;
    }

    public LocalDate parseDate(String raw, String field) {
        try {
            return LocalDate.parse(raw);
        } catch (Exception e) {
            throw new InvalidRowException("Invalid date format for " + field + " field");
        }
    }

    public BigDecimal parseBigDecimal(String raw, String field) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidRowException("Missing value for " + field + " field");
        }
        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException e) {
            throw new InvalidRowException("Invalid decimal format for " + field + " field", e);
        }
    }

    public void checkWeightKgInRange(
            BigDecimal weightKg,
            String field,
            BigDecimal cohortWeightKg
    ) {
        BigDecimal minWeight = cohortWeightKg.subtract(BigDecimal.ONE);
        BigDecimal maxWeight = cohortWeightKg.add(BigDecimal.ONE);

        if (weightKg.compareTo(minWeight) < 0
                || weightKg.compareTo(maxWeight) > 0) {
            throw new InvalidRowException(
                    field + " must be within 1 kg of cohort starting weight"
            );
        }
    }
    public Integer parseIntOrNull(String raw, String field) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            throw new InvalidRowException("Invalid integer format for " + field + " field", e);
        }
    }

    public Integer parseInt(String raw, String field) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidRowException("Missing value for " + field + " field");
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            throw new InvalidRowException("Invalid integer format for " + field + " field", e);
        }
    }

    public void validateCardioFieldsExist(String rawMin, String rawZone) {
        // Either both cardio_min and cardio_zone are present or neither is present
        boolean bothPresent = rawMin != null && rawZone != null;
        boolean neitherPresent = rawMin == null && rawZone == null;
        if (!bothPresent && !neitherPresent) {
            throw new InvalidRowException("Both cardio_min and cardio_zone must be present " +
                    "or neither must be present");
        }
    }

    public CardioZone parseCardioZone(String rawZone, String field) {
        String trimmed = rawZone == null ? "" : rawZone.trim();

        // No cardio has been done today
        if (trimmed.isEmpty()) {
            return CardioZone.NONE;
        }
        try {
            int zone = Integer.parseInt(trimmed);
            return CardioZone.fromInt(zone);
        } catch (NumberFormatException e) {
            throw new InvalidRowException("Invalid cardio zone int for " + field + ": '" + rawZone + "'", e);
        }
    }

    public void validateWorkoutFieldsExist(
            WorkoutType workoutType,
            String rawWorkoutRir,
            String rawTotalSets
    ) {
        // If workout_type is Rest, then workout_rir needs to be empty and total_sets needs to be 0 or empty
        if (workoutType == WorkoutType.REST) {
            if (rawWorkoutRir != null && !rawWorkoutRir.isEmpty()) {
                throw new InvalidRowException("workout_rir must be empty for " + WorkoutType.REST +
                        " workout_type");
            }
            if (rawTotalSets != null && !rawTotalSets.isEmpty()) {
                throw new InvalidRowException("total_sets must be empty for " + WorkoutType.REST +
                        " workout_type");
            }
            // Otherwise, workout_rir and total_sets are required
        } else {
            if (rawWorkoutRir == null
                    || rawWorkoutRir.isEmpty()
                    || rawTotalSets == null
                    || rawTotalSets.isEmpty())
            {
                throw new InvalidRowException("total_sets and workout_rir must not be empty for non-rest " +
                        "workout_type");
            }
        }
    }

    public WorkoutType parseWorkoutType(String raw, String field) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidRowException("Missing value for " + field + " field");
        }
        try {
            return WorkoutType.valueOf(trimmed.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRowException("Invalid workout type for " + field + ": '" + raw + "'", e);
        }
    }

    public WorkoutRir parseWorkoutRir(String raw, String field) {
        try {
            return WorkoutRir.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new InvalidRowException("Invalid workout RIR for " + field + ": '" + raw + "'", e);
        }
    }

}