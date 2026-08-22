package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.exception.PdfGenerationException;
import com.bryanhuang.workflow.model.CohortProfile;
import com.bryanhuang.workflow.model.workout.CohortAnalysisResult;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    private final TemplateEngine pdfTemplateEngine;

    public void generatePdf(CohortProfile cohortProfile, CohortAnalysisResult.Report report,
                            String narrative, String outputPath) {
        String html = renderHtml(cohortProfile, report, narrative);
        writePdfAtomically(html, outputPath);
    }

    private String renderHtml(CohortProfile cohortProfile, CohortAnalysisResult.Report report, String narrative) {
        // Put objects into Thymeleaf context then get the HTML string
        Context context = new Context();
        context.setVariable("cohortProfile", cohortProfile);
        context.setVariable("report", report);
        context.setVariable("narrative", narrative);
        return pdfTemplateEngine.process("report", context);
    }

    private void writePdfAtomically(String html, String outputPath) {
        Path target = Path.of(outputPath);
        // Write into this temporary file first, then move it to the final path in case something goes wrong
        Path tempFile;
        try {
            Files.createDirectories(target.getParent());
            tempFile = Files.createTempFile(target.getParent(), "report-", ".pdf.tmp");
        } catch (IOException e) {
            throw new PdfGenerationException("Failed to prepare temp file for PDF output", e);
        }

        try (OutputStream os = Files.newOutputStream(tempFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            deleteQuietly(tempFile);
            throw new PdfGenerationException("Failed to render PDF", e);
        }

        try {
            Files.move(tempFile, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            deleteQuietly(tempFile);
            throw new PdfGenerationException("Failed to move PDF into place", e);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to clean up temp file: {}", path, e);
        }
    }
}