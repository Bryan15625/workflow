package com.bryanhuang.workflow.model.workout;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class CohortAnalysisResult {

    @Getter
    @Builder
    public static class OverallResult {
        private final String basis; // always "end_weight", documents what the flag is derived from
        private final int passedCount;
        private final int failedCount;
    }

    @Getter
    @Builder
    public static class PassedBlock {
        private final long number;
        private final Double averageValue; // null if nobody passed
        private final String description;
    }

    @Getter
    @Builder
    public static class FailedBlock {
        private final long numberAboveIdeal;
        private final long numberBelowIdeal;
        private final Double averageDeviationAboveIdeal; // null if nobody failed above
        private final Double averageDeviationBelowIdeal; // null if nobody failed below
        private final String aboveDescription;          // null if nobody failed above
        private final String belowDescription;          // null if nobody failed below
    }

    @Getter
    @Builder
    public static class MetricResult {
        private final String id;
        private final String title;
        private final String description;
        private final PassedBlock passed;
        private final FailedBlock failed;
    }

    @Getter
    @Builder
    public static class Report {
        private final OverallResult overallResult;
        private final List<MetricResult> metrics;
    }
}