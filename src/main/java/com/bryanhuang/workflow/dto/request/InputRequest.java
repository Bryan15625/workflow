package com.bryanhuang.workflow.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InputRequest {
    @NotBlank(message = "Source file path is required and cannot be blank")
    private String sourceFilePath;

    @NotBlank(message = "Result file path is required and cannot be blank")
    private String resultFilePath;
}
