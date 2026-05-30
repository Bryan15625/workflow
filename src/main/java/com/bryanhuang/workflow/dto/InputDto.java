package com.bryanhuang.workflow.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
public class InputDto {
    @NotBlank(message = "Source file path is required and cannot be blank")
    private String sourceFilePath;

    @NotBlank(message = "Result file path is required and cannot be blank")
    private String resultFilePath;

    public InputDto(
            String sourceFilePath,
            String resultFilePath
    ) {
        this.sourceFilePath = sourceFilePath;
        this.resultFilePath = resultFilePath;
    }
}
