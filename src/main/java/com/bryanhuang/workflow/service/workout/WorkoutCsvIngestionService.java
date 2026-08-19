package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutRecordEntity;
import com.bryanhuang.workflow.exception.InvalidRowException;
import com.bryanhuang.workflow.mapper.WorkoutRecordEntityMapper;
import com.bryanhuang.workflow.model.CohortProfile;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.model.workout.CardioZone;
import com.bryanhuang.workflow.model.workout.WorkoutRecord;
import com.bryanhuang.workflow.model.workout.WorkoutRir;
import com.bryanhuang.workflow.model.workout.WorkoutType;
import com.bryanhuang.workflow.repository.WorkoutRecordRepository;
import com.bryanhuang.workflow.service.workflow.WorkflowControlGate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutCsvIngestionService {

    private final WorkoutRecordRepository workoutRecordRepository;
    private final WorkoutRecordEntityMapper workoutRecordEntityMapper;
    private final WorkoutValidationService validationService;
    private final WorkflowControlGate workflowControlGate;

    private static final int BATCH_SIZE = 10_000;

    public JobControl ingestCsv(Step step, Workflow workflow, WorkflowExecutionEntity entity)
            throws InterruptedException {
        UUID workflowExecutionId = entity.getWorkflowExecutionId();
        log.info("Parsing CSV file for workflow execution: {}", workflowExecutionId);
        String fp = "/data/input/" + workflow.getData().getInput();

        List<WorkoutRecordEntity> batch = new ArrayList<>(BATCH_SIZE);
        String currentUserId = null;
        int currentUserRowCount = 0;
        BigDecimal cohortWeightKg = BigDecimal.valueOf(workflow.getCohortProfile().getWeightKg());
        int expectedDurationDays = workflow.getCohortProfile().getDurationDays();
        int participantCount = 0;

        BufferedReader reader = null;
        try {
            reader = Files.newBufferedReader(Path.of(fp));
            // Skip the header line
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                WorkoutRecord record = validateAndCreateWorkoutRecord(line);

                // Validate weight for each user and number of rows per user (must match cohortProfile)
                if (!record.getUserId().equals(currentUserId)) {
                    if (currentUserId != null
                            && currentUserRowCount != expectedDurationDays) {

                        throw new InvalidRowException(
                                "User " + currentUserId
                                        + " has " + currentUserRowCount
                                        + " rows, expected "
                                        + expectedDurationDays
                        );
                    }

                    // Check the new user and ensure their weight is within the cohort's range
                    currentUserId = record.getUserId();
                    currentUserRowCount = 0;
                    participantCount++;

                    validationService.checkWeightKgInRange(
                            record.getWeightKg(),
                            "weight_kg",
                            cohortWeightKg
                    );
                }
                currentUserRowCount++;


                WorkoutRecordEntity recordEntity = workoutRecordEntityMapper
                        .toWorkoutRecordEntity(record, entity);
                batch.add(recordEntity);
                if (batch.size() >= BATCH_SIZE) {
                    log.info("Saving batch of {} records", batch.size());
                    workoutRecordRepository.saveAll(batch);
                    batch.clear();

                    // Check if any signal has been received mid-file
                    if (workflowControlGate.checkpointStep(workflowExecutionId, step.getStepId())
                            == JobControl.TERMINATE) {
                        log.info("CSV parsing terminated mid-file: {}", workflowExecutionId);
                        return JobControl.TERMINATE;
                    }
                }
            }
            // Validate the last user
            if (currentUserId != null
                    && currentUserRowCount != expectedDurationDays) {

                throw new InvalidRowException(
                        "User " + currentUserId
                                + " has " + currentUserRowCount
                                + " rows, expected "
                                + expectedDurationDays
                );
            }
            // Check to see if the number of participants matches cohortProfile
            if (participantCount != workflow.getCohortProfile().getParticipants()) {
                throw new InvalidRowException(
                        "Found " + participantCount
                                + " participants, expected "
                                + workflow.getCohortProfile().getParticipants()
                );
            }
            // Save any remaining records
            if (!batch.isEmpty()) {
                log.info("Saving batch of {} records", batch.size());
                workoutRecordRepository.saveAll(batch);
            }
            log.info("CSV file parsing completed");
        } catch (IOException e) {
            log.error("Error reading file", e);
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Failed to close CSV reader", e);
                }
            }
        }
        return JobControl.NONE;
    }

    private WorkoutRecord validateAndCreateWorkoutRecord(String line) {
        String[] components = line.split(",");
        if (components.length != 14) {
            throw new InvalidRowException("Invalid CSV line format");
        }

        // These fields must all be present
        String userId = validationService.parseUserId(components[0], "user_id");
        LocalDate date = validationService.parseDate(components[1], "date");
        BigDecimal weightKg = validationService.parseBigDecimal(components[2], "weight_kg");
        Integer calories = validationService.parseInt(components[3], "calories");
        Integer proteinG = validationService.parseInt(components[4], "protein_g");
        Integer carbsG = validationService.parseInt(components[5], "carbs_g");
        Integer fatsG = validationService.parseInt(components[6], "fats_g");
        Integer steps = validationService.parseInt(components[9], "steps");
        WorkoutType workoutType = validationService.parseWorkoutType(components[10], "workout_type");
        BigDecimal sleepH = validationService.parseBigDecimal(components[13], "sleep_h");

        // Ensure cardio fields exist before parsing
        validationService.validateCardioFieldsExist(components[7], components[8]);
        Integer cardioMin = validationService.parseInt(components[7], "cardio_min");
        CardioZone cardioZone = validationService.parseCardioZone(components[8], "cardio_zone");

        // Ensure workout fields exist before parsing the rest
        validationService.validateWorkoutFieldsExist(workoutType, components[11], components[12]);
        WorkoutRir workoutRir = validationService.parseWorkoutRir(components[11], "workout_rir");
        Integer totalSets = validationService.parseIntOrNull(components[12], "total_sets");


        return WorkoutRecord.builder()
                .userId(userId)
                .date(date)
                .weightKg(weightKg)
                .calories(calories)
                .proteinG(proteinG)
                .carbsG(carbsG)
                .fatsG(fatsG)
                .cardioMin(cardioMin)
                .cardioZone(cardioZone)
                .steps(steps)
                .workoutType(workoutType)
                .workoutRir(workoutRir)
                .totalSets(totalSets)
                .sleepH(sleepH)
                .build();
    }
}
