package com.bryanhuang.workflow.model;

public enum StepName {
    DOWNLOAD_FILE,
    PARSE_CSV,
    VALIDATE_ROWS,
    GENERATE_STATS,
    DETECT_ISSUES,
    GENERATE_REPORT,
    UPLOAD_RESULT
}
