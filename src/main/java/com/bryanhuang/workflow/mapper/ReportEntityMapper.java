package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.*;
import com.bryanhuang.workflow.model.workout.CohortAnalysisResult;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ReportEntityMapper {

    public ReportEntity toReportEntity(CohortAnalysisResult.Report report, WorkflowExecutionEntity entity) {
        return ReportEntity.builder()
                .workflowExecutionEntity(entity)
                .overallResult(toOverallResultEntity(report.getOverallResult()))
                .metrics(toMetricResultEntities(report.getMetrics()))
                .build();
    }

    private OverallResultEntity toOverallResultEntity(
            CohortAnalysisResult.OverallResult overallResult
    ) {
        if (overallResult == null) {
            return null;
        }
        return OverallResultEntity.builder()
                .basis(overallResult.getBasis())
                .passedCount(overallResult.getPassedCount())
                .failedCount(overallResult.getFailedCount())
                .build();
    }

    private List<MetricResultEntity> toMetricResultEntities(List<CohortAnalysisResult.MetricResult> metrics) {
        return metrics.stream()
                .map(metric -> MetricResultEntity.builder()
                        .metricId(metric.getId())
                        .title(metric.getTitle())
                        .description(metric.getDescription())
                        .passed(toPassedBlockEntity(metric.getPassed()))
                        .failed(toFailedBlockEntity(metric.getFailed()))
                        .build())
                .toList();
    }

    private PassedBlockEntity toPassedBlockEntity(CohortAnalysisResult.PassedBlock passed) {
        if (passed == null) {
            return null;
        }
        return PassedBlockEntity.builder()
                .number(passed.getNumber())
                .averageValue(passed.getAverageValue())
                .description(passed.getDescription())
                .build();
    }

    private FailedBlockEntity toFailedBlockEntity(CohortAnalysisResult.FailedBlock failed) {
        if (failed == null) {
            return null;
        }
        return FailedBlockEntity.builder()
                .numberAboveIdeal(failed.getNumberAboveIdeal())
                .numberBelowIdeal(failed.getNumberBelowIdeal())
                .averageDeviationAboveIdeal(failed.getAverageDeviationAboveIdeal())
                .averageDeviationBelowIdeal(failed.getAverageDeviationBelowIdeal())
                .aboveDescription(failed.getAboveDescription())
                .belowDescription(failed.getBelowDescription())
                .build();
    }

    public CohortAnalysisResult.Report toReport(
            ReportEntity reportEntity
    ) {
        return CohortAnalysisResult.Report.builder()
                .overallResult(toOverallResult(reportEntity.getOverallResult()))
                .metrics(toMetricResults(reportEntity.getMetrics()))
                .build();
    }

    private CohortAnalysisResult.OverallResult toOverallResult(OverallResultEntity entity) {
        if (entity == null) {
            return null;
        }
        return CohortAnalysisResult.OverallResult.builder()
                .basis(entity.getBasis())
                .passedCount(entity.getPassedCount())
                .failedCount(entity.getFailedCount())
                .build();
    }

    private List<CohortAnalysisResult.MetricResult> toMetricResults(List<MetricResultEntity> entities) {
        return entities.stream()
                .map(entity -> CohortAnalysisResult.MetricResult.builder()
                        .id(entity.getMetricId())
                        .title(entity.getTitle())
                        .description(entity.getDescription())
                        .passed(toPassedBlock(entity.getPassed()))
                        .failed(toFailedBlock(entity.getFailed())).build()
        ).toList();
    }

    private CohortAnalysisResult.PassedBlock toPassedBlock(PassedBlockEntity entity) {
        if (entity == null) {
            return null;
        }
        return CohortAnalysisResult.PassedBlock.builder()
                .number(entity.getNumber())
                .averageValue(entity.getAverageValue())
                .description(entity.getDescription())
                .build();
    }

    private CohortAnalysisResult.FailedBlock toFailedBlock(FailedBlockEntity entity) {
        if (entity == null) {
            return null;
        }
        return CohortAnalysisResult.FailedBlock.builder()
                .numberAboveIdeal(entity.getNumberAboveIdeal())
                .numberBelowIdeal(entity.getNumberBelowIdeal())
                .averageDeviationAboveIdeal(entity.getAverageDeviationAboveIdeal())
                .averageDeviationBelowIdeal(entity.getAverageDeviationBelowIdeal())
                .aboveDescription(entity.getAboveDescription())
                .belowDescription(entity.getBelowDescription())
                .build();
    }

}