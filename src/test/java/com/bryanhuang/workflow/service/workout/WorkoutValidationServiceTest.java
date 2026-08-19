package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.exception.InvalidRowException;
import com.bryanhuang.workflow.model.workout.CardioZone;
import com.bryanhuang.workflow.model.workout.WorkoutRir;
import com.bryanhuang.workflow.model.workout.WorkoutType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkoutValidationServiceTest {

    private final WorkoutValidationService validationService = new WorkoutValidationService();

    @Nested
    @DisplayName("parseUserIdTests")
    class ParseUserIdTests {

        @Test
        void returnsRawUserId_WhenUserIdIsPresent() {
            String result = validationService.parseUserId("user-123", "user_id");
            assertEquals("user-123", result);
        }

        @Test
        void throwsException_WhenUserIdIsBlank() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseUserId("   ", "user_id"));
        }

        @Test
        void throwsException_WhenUserIdIsNull() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseUserId(null, "user_id"));
        }

    }

    @Nested
    @DisplayName("parseDateTests")
    class parseDateTests {

        @Test
        void returnsDate_WhenDateIsPresent() {
            LocalDate result = validationService.parseDate("2024-01-15", "date");
            assertEquals(LocalDate.of(2024, 1, 15), result);
        }

        @Test
        void throwsException_WhenDateIsBlank() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseDate("", "date"));
        }

        @Test
        void throwsException_WhenRawIsNotParsableToDate() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseDate("not-a-date", "date"));
        }

    }

    @Nested
    @DisplayName("parseBigDecimalTests")
    class parseBigDecimalTests {

        @Test
        void returnsBigDecimal_WhenBigDecimalIsPresent() {
            BigDecimal result = validationService.parseBigDecimal("72.5", "weight_kg");
            assertEquals(new BigDecimal("72.5"), result);
        }

        @Test
        void throwsException_WhenBigDecimalIsEmpty() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseBigDecimal("   ", "weight_kg"));
        }

        @Test
        void throwsException_WhenBigDecimalIsNull() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseBigDecimal(null, "weight_kg"));
        }

        @Test
        void throwsException_WhenRawIsNotParsableToBigDecimal() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseBigDecimal("abc", "weight_kg"));
        }

    }

    @Nested
    @DisplayName("checkWeightKgInRangeTests")
    class CheckWeightKgInRangeTests {

        @Test
        void returns_whenWeightKgInRange() {
            validationService.checkWeightKgInRange(BigDecimal.valueOf(69), "weight_kg", BigDecimal.valueOf(70));
            validationService.checkWeightKgInRange(BigDecimal.valueOf(70), "weight_kg", BigDecimal.valueOf(70));
            validationService.checkWeightKgInRange(BigDecimal.valueOf(71), "weight_kg", BigDecimal.valueOf(70));

        }

        @Test
        void throwsException_whenWeightKgOutOfRange() {
            assertThrows(
                    InvalidRowException.class,
                    () -> validationService.checkWeightKgInRange(BigDecimal.valueOf(68), "weight_kg",
                            BigDecimal.valueOf(70))
            );
            assertThrows(
                    InvalidRowException.class,
                    () -> validationService.checkWeightKgInRange(BigDecimal.valueOf(72), "weight_kg",
                            BigDecimal.valueOf(70))
            );
        }
    }

    @Nested
    @DisplayName("parseIntOrNullTests")
    class ParseIntOrNullTests {

        @Test
        void returnsInt_WhenIntIsPresent() {
            Integer result = validationService.parseIntOrNull("42", "total_sets");
            assertEquals(42, result);
        }

        @Test
        void returnsNull_WhenIntIsEmpty() {
            Integer result = validationService.parseIntOrNull("   ", "total_sets");
            assertNull(result);
        }

        @Test
        void returnsNull_WhenIntIsNull() {
            Integer result = validationService.parseIntOrNull(null, "total_sets");
            assertNull(result);
        }

        @Test
        void throwsException_WhenRawIsNotParsableToInt() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseIntOrNull("abc", "total_sets"));
        }

    }

    @Nested
    @DisplayName("parseIntTests")
    class ParseIntTests {

        @Test
        void returnsInt_WhenIntIsPresent() {
            Integer result = validationService.parseInt("10", "calories");
            assertEquals(10, result);
        }

        @Test
        void throwsException_WhenIntIsEmpty() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseInt("   ", "calories"));
        }

        @Test
        void throwsException_WhenIntIsNull() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseInt(null, "calories"));
        }

        @Test
        void throwsException_WhenRawIsNotParsableToInt() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseInt("abc", "calories"));
        }

    }

    @Nested
    @DisplayName("validateCardioFieldsExist")
    class ValidateCardioFieldsExistTests {

        @Test
        void returns_WhenAllCardioFieldsArePresent() {
            validationService.validateCardioFieldsExist("30", "2");
            // no exception thrown = pass
        }

        @Test
        void returns_WhenCardioFieldsAreBothMissing() {
            validationService.validateCardioFieldsExist(null, null);
            // no exception thrown = pass
        }

        @Test
        void throwsException_WhenOnlyOneCardioFieldIsMissing() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.validateCardioFieldsExist("30", null));
            assertThrows(InvalidRowException.class,
                    () -> validationService.validateCardioFieldsExist(null, "2"));
        }
    }

    @Nested
    @DisplayName("parseCardioZoneTests")
    class ParseCardioZoneTests {

        @Test
        void returnsCardioZone_WhenCardioZoneIsPresent() {
            CardioZone result = validationService.parseCardioZone("2", "cardio_zone");
            assertEquals(CardioZone.TWO, result);
        }

        @Test
        void returnsCardioZoneNone_WhenCardioZoneIsEmpty() {
            CardioZone result = validationService.parseCardioZone("   ", "cardio_zone");
            assertEquals(CardioZone.NONE, result);
        }

        @Test
        void returnsCardioZoneNone_WhenCardioZoneIsNull() {
            CardioZone result = validationService.parseCardioZone(null, "cardio_zone");
            assertEquals(CardioZone.NONE, result);
        }

        @Test
        void throwsException_WhenRawIsNotParsableToCardioZone() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseCardioZone("abc", "cardio_zone"));
        }

        @Test
        void throwsException_WhenCardioZoneIntOutOfRange() {
            // CardioZone.fromInt only accepts 1-5; 0 and 6+ should be rejected.
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseCardioZone("0", "cardio_zone"));
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseCardioZone("6", "cardio_zone"));
        }

    }

    @Nested
    @DisplayName("validateWorkoutFieldsExistTests")
    class ValidateWorkoutFieldsExistTests {

        @Test
        void returns_WhenAllWorkoutFieldsArePresent() {
            validationService.validateWorkoutFieldsExist(WorkoutType.PUSH, "2", "5");
            // no exception thrown = pass
        }

        @Test
        void returns_WhenWorkoutTypeIsRestAndFieldsAreEmpty() {
            validationService.validateWorkoutFieldsExist(WorkoutType.REST, "", "");
            validationService.validateWorkoutFieldsExist(WorkoutType.REST, null, null);
            // no exception thrown = pass
        }

        @Test
        void throwsException_WhenWorkoutRirPresent_WorkoutTypeIsRest() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.validateWorkoutFieldsExist(WorkoutType.REST, "2", ""));
        }

        @Test
        void throwsException_WhenTotalSetsPresent_WorkoutTypeIsRest() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.validateWorkoutFieldsExist(WorkoutType.REST, "", "5"));
        }

        @Test
        void throwsException_WhenFieldsNotPresent_WorkoutTypeIsNotRest() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.validateWorkoutFieldsExist(WorkoutType.PUSH, "", ""));
            assertThrows(InvalidRowException.class,
                    () -> validationService.validateWorkoutFieldsExist(WorkoutType.PUSH, null, "5"));
            assertThrows(InvalidRowException.class,
                    () -> validationService.validateWorkoutFieldsExist(WorkoutType.PUSH, "2", null));
            assertThrows(InvalidRowException.class,
                    () -> validationService.validateWorkoutFieldsExist(WorkoutType.PUSH, "2", ""));
        }
    }

    @Nested
    @DisplayName("parseWorkoutTypeTests")
    class ParseWorkoutTypeTests {

        @Test
        void returnsWorkoutType_WhenWorkoutTypeIsPresent() {
            WorkoutType result = validationService.parseWorkoutType("push", "workout_type");
            assertEquals(WorkoutType.PUSH, result);
        }

        @Test
        void throwsException_WhenWorkoutTypeIsEmptyOrNull() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseWorkoutType("   ", "workout_type"));

            assertThrows(InvalidRowException.class,
                    () -> validationService.parseWorkoutType(null, "workout_type"));
        }

        @Test
        void throwsException_WhenRawIsNotParsableToWorkoutType() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseWorkoutType("bench", "workout_type"));
        }
    }

    @Nested
    @DisplayName("parseWorkoutRirTests")
    class ParseWorkoutRirTests {

        @Test
        void returnsWorkoutRir_WhenWorkoutRirIsPresent() {
            WorkoutRir result = validationService.parseWorkoutRir("3", "workout_rir");
            assertEquals(WorkoutRir.THREE, result);
        }

        @Test
        void returnsWorkoutRirNone_WhenWorkoutRirIsEmpty() {
            // WorkoutRir.fromString treats null/blank as NONE rather than throwing.
            WorkoutRir result = validationService.parseWorkoutRir(null, "workout_rir");
            WorkoutRir result2 = validationService.parseWorkoutRir(" ", "workout_rir");
            assertEquals(WorkoutRir.NONE, result);
            assertEquals(WorkoutRir.NONE, result2);
        }

        @Test
        void throwsException_WhenWorkoutRirIsNotParsableToWorkoutRir() {
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseWorkoutRir("abc", "workout_rir"));
        }

        @Test
        void throwsException_WhenWorkoutRirIntOutOfRange() {
            // WorkoutRir.fromInt only accepts 0-10.
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseWorkoutRir("11", "workout_rir"));
            assertThrows(InvalidRowException.class,
                    () -> validationService.parseWorkoutRir("-1", "workout_rir"));
        }
    }

}