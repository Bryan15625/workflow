package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.ReportEntity;
import com.bryanhuang.workflow.model.workout.CohortAnalysisResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReportEntityMapperTest {

    private final ReportEntityMapper mapper = new ReportEntityMapper();

    @Nested
    @DisplayName("toReportEntity()")
    class ToReportEntityTests {

        @Test
        @DisplayName("maps report to report entity")
        void mapsReportToReportEntity() {
            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            CohortAnalysisResult.Report report = buildReport();

            ReportEntity result = mapper.toReportEntity(report, workflowExecutionEntity);

            assertNotNull(result);
            assertEquals(workflowExecutionEntity, result.getWorkflowExecutionEntity());

            // Overall result
            assertNotNull(result.getOverallResult());
            assertEquals(report.getOverallResult().getBasis(), result.getOverallResult().getBasis());
            assertEquals(report.getOverallResult().getPassedCount(), result.getOverallResult().getPassedCount());
            assertEquals(report.getOverallResult().getFailedCount(), result.getOverallResult().getFailedCount());

            // Metrics
            assertEquals(report.getMetrics().size(), result.getMetrics().size());

            var metric = result.getMetrics().getFirst();
            var reportMetricFirst = report.getMetrics().getFirst();

            assertEquals(reportMetricFirst.getId(), metric.getMetricId());
            assertEquals(reportMetricFirst.getTitle(), metric.getTitle());
            assertEquals(reportMetricFirst.getDescription(), metric.getDescription());

            // Passed block
            assertNotNull(metric.getPassed());
            assertEquals(reportMetricFirst.getPassed().getNumber(), metric.getPassed().getNumber());
            assertEquals(reportMetricFirst.getPassed().getAverageValue(), metric.getPassed().getAverageValue());
            assertEquals(reportMetricFirst.getPassed().getDescription(), metric.getPassed().getDescription());

            // Failed block
            assertNotNull(metric.getFailed());
            assertEquals(reportMetricFirst.getFailed().getNumberAboveIdeal(), metric.getFailed().getNumberAboveIdeal());
            assertEquals(reportMetricFirst.getFailed().getNumberBelowIdeal(), metric.getFailed().getNumberBelowIdeal());
            assertEquals(reportMetricFirst.getFailed().getAverageDeviationAboveIdeal(),
                    metric.getFailed().getAverageDeviationAboveIdeal());
            assertEquals(reportMetricFirst.getFailed().getAverageDeviationBelowIdeal(),
                    metric.getFailed().getAverageDeviationBelowIdeal());
            assertEquals(reportMetricFirst.getFailed().getAboveDescription(), metric.getFailed().getAboveDescription());
            assertEquals(reportMetricFirst.getFailed().getBelowDescription(), metric.getFailed().getBelowDescription());
        }

        @Test
        @DisplayName("maps report to report entity with null fields")
        void mapsReportToReportEntityWithNullFields() {
            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            CohortAnalysisResult.Report report =
                    CohortAnalysisResult.Report.builder()
                            .overallResult(null)
                            .metrics(List.of(CohortAnalysisResult.MetricResult.builder()
                                    .passed(null)
                                    .failed(null)
                                    .build()))
                            .build();

            ReportEntity result = mapper.toReportEntity(report, workflowExecutionEntity);
            assertNotNull(result);
            assertEquals(workflowExecutionEntity, result.getWorkflowExecutionEntity());
            assertNull(result.getOverallResult());
            assertNull(result.getMetrics().getFirst().getPassed());
            assertNull(result.getMetrics().getFirst().getFailed());
        }
    }

    @Nested
    @DisplayName("toReport()")
    class ToReportTests {

        @Test
        @DisplayName("maps report entity to report")
        void mapsReportEntityToReport() {
            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            ReportEntity entity = mapper.toReportEntity(buildReport(), workflowExecutionEntity);

            CohortAnalysisResult.Report result = mapper.toReport(entity);

            assertNotNull(result);

            // Overall result
            assertNotNull(result.getOverallResult());
            assertEquals(entity.getOverallResult().getBasis(), result.getOverallResult().getBasis());
            assertEquals(entity.getOverallResult().getPassedCount(), result.getOverallResult().getPassedCount());
            assertEquals(entity.getOverallResult().getFailedCount(), result.getOverallResult().getFailedCount());

            // Metrics
            assertEquals(entity.getMetrics().size(), result.getMetrics().size());

            var metric = result.getMetrics().getFirst();
            var entityMetric = entity.getMetrics().getFirst();

            assertEquals(entityMetric.getMetricId(), metric.getId());
            assertEquals(entityMetric.getTitle(), metric.getTitle());
            assertEquals(entityMetric.getDescription(), metric.getDescription());

            // Passed block
            assertNotNull(metric.getPassed());
            assertEquals(entityMetric.getPassed().getNumber(), metric.getPassed().getNumber());
            assertEquals(entityMetric.getPassed().getAverageValue(), metric.getPassed().getAverageValue());
            assertEquals(entityMetric.getPassed().getDescription(), metric.getPassed().getDescription());

            // Failed block
            assertNotNull(metric.getFailed());
            assertEquals(entityMetric.getFailed().getNumberAboveIdeal(), metric.getFailed().getNumberAboveIdeal());
            assertEquals(entityMetric.getFailed().getNumberBelowIdeal(), metric.getFailed().getNumberBelowIdeal());
            assertEquals(entityMetric.getFailed().getAverageDeviationAboveIdeal(),
                    metric.getFailed().getAverageDeviationAboveIdeal());
            assertEquals(entityMetric.getFailed().getAverageDeviationBelowIdeal(),
                    metric.getFailed().getAverageDeviationBelowIdeal());
            assertEquals(entityMetric.getFailed().getAboveDescription(), metric.getFailed().getAboveDescription());
            assertEquals(entityMetric.getFailed().getBelowDescription(), metric.getFailed().getBelowDescription());
        }

        @Test
        @DisplayName("maps report entity to report with null fields")
        void mapsReportEntityToReportWithNullFields() {

            CohortAnalysisResult.Report report =
                    CohortAnalysisResult.Report.builder()
                            .overallResult(null)
                            .metrics(List.of(CohortAnalysisResult.MetricResult.builder()
                                    .passed(null)
                                    .failed(null)
                                    .build()))
                            .build();

            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            ReportEntity entity = mapper.toReportEntity(report, workflowExecutionEntity);

            CohortAnalysisResult.Report result = mapper.toReport(entity);

            assertNotNull(result);
            assertNull(result.getOverallResult());
            assertNull(result.getMetrics().getFirst().getPassed());
            assertNull(result.getMetrics().getFirst().getFailed());
        }
    }

    private CohortAnalysisResult.Report buildReport() {
        CohortAnalysisResult.OverallResult overallResult =
                CohortAnalysisResult.OverallResult.builder()
                        .basis("end_weight")
                        .passedCount(8)
                        .failedCount(2)
                        .build();

        CohortAnalysisResult.PassedBlock passed =
                CohortAnalysisResult.PassedBlock.builder()
                        .number(8)
                        .averageValue(140.5)
                        .description(
                                "Average protein intake supported muscle repair and retention."
                        )
                        .build();

        CohortAnalysisResult.FailedBlock failed =
                CohortAnalysisResult.FailedBlock.builder()
                        .numberAboveIdeal(1)
                        .numberBelowIdeal(1)
                        .averageDeviationAboveIdeal(15.5)
                        .averageDeviationBelowIdeal(10.0)
                        .aboveDescription(
                                "Protein intake exceeded the recommended range."
                        )
                        .belowDescription(
                                "Protein intake fell short of the recommended range."
                        )
                        .build();

        CohortAnalysisResult.MetricResult metric =
                CohortAnalysisResult.MetricResult.builder()
                        .id("protein")
                        .title("Daily Protein Goal")
                        .description(
                                "Average daily protein intake compared to the recommended range."
                        )
                        .passed(passed)
                        .failed(failed)
                        .build();

        return CohortAnalysisResult.Report.builder()
                .overallResult(overallResult)
                .metrics(List.of(metric))
                .build();
    }
}