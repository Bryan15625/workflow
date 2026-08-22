package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.ReportEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutIdealEntity;
import com.bryanhuang.workflow.exception.ReportNotFoundException;
import com.bryanhuang.workflow.exception.WorkoutIdealNotFoundException;
import com.bryanhuang.workflow.mapper.ReportEntityMapper;
import com.bryanhuang.workflow.mapper.WorkoutIdealEntityMapper;
import com.bryanhuang.workflow.model.CohortProfile;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.model.workout.CohortAnalysisResult;
import com.bryanhuang.workflow.model.workout.WorkoutIdeal;
import com.bryanhuang.workflow.repository.ReportRepository;
import com.bryanhuang.workflow.repository.WorkoutIdealRepository;
import com.bryanhuang.workflow.service.workflow.WorkflowControlGate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutSummaryGenerationService {

    private final ReportRepository reportRepository;
    private final ReportEntityMapper reportEntityMapper;
    private final WorkflowControlGate workflowControlGate;
    private final AiResponseService aiResponseService;
    private final PdfGenerationService pdfGenerationService;
    private final ObjectMapper objectMapper;
    private final WorkoutIdealRepository workoutIdealRepository;
    private final WorkoutIdealEntityMapper workoutIdealEntityMapper;

    public JobControl generateSummary(Step step, Workflow workflow, WorkflowExecutionEntity entity)
            throws InterruptedException {
        UUID workflowExecutionId = entity.getWorkflowExecutionId();
        log.info("Generating summary report for workflow execution: {}", workflowExecutionId);
        CohortProfile cohortProfile = workflow.getCohortProfile();

        // Retrieve the report
        ReportEntity reportEntity = reportRepository
                .findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId)
                .orElseThrow(() -> new ReportNotFoundException("Unable to generate summary due to missing report"));
        CohortAnalysisResult.Report report = reportEntityMapper.toReport(reportEntity);

        // Retrieve the workout ideal
        WorkoutIdealEntity idealEntity = workoutIdealRepository
                .findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId)
                .orElseThrow(() -> new WorkoutIdealNotFoundException("Unable to generate summary due to " +
                                "missing workout ideal"));
        WorkoutIdeal ideal = workoutIdealEntityMapper.toWorkoutIdeal(idealEntity);


        // Checkpoint before the LLM call.
        if (workflowControlGate.checkpointStep(workflowExecutionId, step.getStepId()) == JobControl.TERMINATE) {
            log.info("Summary generation terminated before LLM call: {}", workflowExecutionId);
            return JobControl.TERMINATE;
        }

        String prompt = buildPrompt(cohortProfile, report, ideal);
        String narrative = aiResponseService.generateResponse(workflowExecutionId, prompt);
        Thread.sleep(5000);

        // Checkpoint after the LLM call and before PDF generation.
        if (workflowControlGate.checkpointStep(workflowExecutionId, step.getStepId()) == JobControl.TERMINATE) {
            log.info("Summary generation terminated after LLM call, before PDF write: {}", workflowExecutionId);
            return JobControl.TERMINATE;
        }

        String outputPath = "/data/output/" + workflow.getData().getOutput();
        pdfGenerationService.generatePdf(cohortProfile, report, narrative, outputPath);
        Thread.sleep(5000);

        log.info("Summary generation completed: {}", workflowExecutionId);
        return JobControl.NONE;
    }

    private String buildPrompt(CohortProfile cohortProfile, CohortAnalysisResult.Report report, WorkoutIdeal ideal) {
        String reportJson;
        String idealJson;
        try {
            reportJson = objectMapper.writeValueAsString(report);
            idealJson = objectMapper.writeValueAsString(ideal);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize report for LLM prompt", e);
        }

        return """
                You are generating the analytical findings section of a health analytics report for a PDF.
                
                The report is based on a %d-year-old %s cohort, with a height of %d cm,
                starting weight of %d kg, goal of %s, and tracking period of %d days.
                
                The PDF already provides the cohort profile and overall outcome separately.
                Do not repeat or summarize that information in your response.
                
                The first JSON contains the calculated ideal target ranges for this cohort.
                These values are generated by the application's deterministic analytics
                engine and represent the authoritative benchmarks.
                
                The second JSON contains the actual cohort performance and analysis,
                including participant counts, averages, and deviations from the ideal ranges.
                
                Your task is to write the analytical findings by directly comparing the
                observed cohort results against their corresponding ideal targets.
                
                STRUCTURE:
                - Write one paragraph for each significant metric or finding.
                - Each paragraph must focus on exactly one metric or closely related finding.
                - Begin each paragraph with the actual result or key finding.
                - Introduce the relevant ideal range naturally within the paragraph when
                  needed to interpret the result.
                - Explain what the comparison means in relation to the cohort's stated goal.
                - Order the paragraphs from the most important findings to the least important.
                - Do not create a separate paragraph summarizing all ideal ranges.
                - Do not list ideal ranges before discussing the corresponding actual results.
                - Do not group all successful metrics into one paragraph or all failed
                  metrics into another.
                - Do not repeat the cohort profile or overall outcome.
                - Do not provide a general introduction or conclusion.
                - Do not provide any other paragraphs.
                
                WRITING STYLE:
                - Write in a professional, analytical tone suitable for a PDF report.
                - Make the writing read as a cohesive analytical report rather than a list
                  of statistics.
                - Integrate the ideal target into the discussion rather than presenting it
                  as a separate piece of information.
                - Focus on the relationship between the actual result and the ideal target.
                - Use specific participant counts, averages, target ranges, and deviations
                  when they meaningfully support the finding.
                - Explain the significance of each finding in relation to the cohort's goal.
                - Vary paragraph openings naturally. Do not repeatedly begin paragraphs with
                  phrases such as "For", "Regarding", or "In terms of".
                - Avoid simply listing numbers without interpretation.
                - Do not repeat the same metric or finding unnecessarily.
                - Keep each paragraph to approximately 2-4 sentences.
                
                DATA ACCURACY:
                - Treat the provided ideal ranges and actual results as authoritative.
                - Use only values and relationships explicitly present in the provided JSON.
                - Do not calculate, estimate, or invent values that are not present.
                - Do not independently determine or suggest alternative ideal ranges.
                - Do not introduce external health, fitness, nutrition, or medical guidelines.
                - Do not make medical diagnoses, medical recommendations, or claims about
                  health effects that are not directly supported by the provided data.
                - Do not describe a metric as "healthy", "unhealthy", "safe", "unsafe",
                  "optimal", or similar unless that characterization is explicitly supported
                  by the provided data.
                - When interpreting a metric, limit the explanation to what can reasonably
                  be concluded from its relationship to the application's ideal target and
                  the cohort's stated goal.
                
                OUTPUT:
                - Return only the analytical findings.
                - Do not use Markdown headings, bullet points, tables, numbered lists, or JSON.
                - Do not include labels such as "Analysis:" or "Findings:".
                - Write approximately one paragraph per significant metric or finding.
                
                Ideal target ranges:
                %s
                
                Actual cohort analysis:
                %s
            """.formatted(
                    cohortProfile.getAge(),
                    cohortProfile.getSex(),
                    cohortProfile.getHeightCm(),
                    cohortProfile.getWeightKg(),
                    cohortProfile.getGoal(),
                    cohortProfile.getDurationDays(),
                    idealJson,
                    reportJson
        );
    }
}