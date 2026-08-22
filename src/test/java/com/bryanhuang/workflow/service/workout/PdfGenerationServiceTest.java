package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.exception.PdfGenerationException;
import com.bryanhuang.workflow.model.CohortProfile;
import com.bryanhuang.workflow.model.workout.CohortAnalysisResult;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceTest {

    private static final String MINIMAL_VALID_XHTML = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"/></head>
            <body><p>Report body</p></body>
            </html>
            """;

    @TempDir
    Path tempDir;

    @Mock
    private TemplateEngine pdfTemplateEngine;

    @InjectMocks
    private PdfGenerationService pdfGenerationService;

    private CohortProfile cohortProfile;
    private CohortAnalysisResult.Report report;
    private String narrative;

    @BeforeEach
    void setUp() {
        cohortProfile = mock(CohortProfile.class);
        report = mock(CohortAnalysisResult.Report.class);
        narrative = "some narrative text";
    }

    @Nested
    @DisplayName("renderHtml() (via generatePdf)")
    class RenderHtmlTests {

        @Test
        @DisplayName("passes cohortProfile, report, and narrative into the Thymeleaf context under the expected names")
        void wiresTemplateVariablesCorrectly() {
            when(pdfTemplateEngine.process(eq("report"), any(Context.class))).thenReturn(MINIMAL_VALID_XHTML);
            String outputPath = tempDir.resolve("report.pdf").toString();

            pdfGenerationService.generatePdf(cohortProfile, report, narrative, outputPath);

            ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
            verify(pdfTemplateEngine).process(eq("report"), contextCaptor.capture());
            Context capturedContext = contextCaptor.getValue();

            assertThat(capturedContext.getVariable("cohortProfile")).isEqualTo(cohortProfile);
            assertThat(capturedContext.getVariable("report")).isEqualTo(report);
            assertThat(capturedContext.getVariable("narrative")).isEqualTo(narrative);
        }
    }

    @Nested
    @DisplayName("generatePdf() - happy path")
    class HappyPathTests {

        @Test
        @DisplayName("writes a real, valid PDF file to the target output path")
        void generatesPdfSuccessfully() throws IOException {
            when(pdfTemplateEngine.process(eq("report"), any(Context.class))).thenReturn(MINIMAL_VALID_XHTML);
            Path target = tempDir.resolve("nested/dir/report.pdf");

            pdfGenerationService.generatePdf(cohortProfile, report, narrative, target.toString());

            assertThat(target).exists();
            byte[] header = Files.readAllBytes(target);
            // pdf needs to start with %PDF- for it to be a valid pdf
            assertThat(new String(header, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        }

        @Test
        @DisplayName("creates parent directories that don't yet exist")
        void createsMissingParentDirectories() {
            when(pdfTemplateEngine.process(eq("report"), any(Context.class))).thenReturn(MINIMAL_VALID_XHTML);
            Path target = tempDir.resolve("does/not/exist/yet/report.pdf");
            assertThat(target.getParent()).doesNotExist();

            pdfGenerationService.generatePdf(cohortProfile, report, narrative, target.toString());

            assertThat(target).exists();
        }

        @Test
        @DisplayName("does not leave a .pdf.tmp temp file behind after a successful move")
        void cleansUpTempFileOnSuccess() throws IOException {
            when(pdfTemplateEngine.process(eq("report"), any(Context.class)))
                    .thenReturn(MINIMAL_VALID_XHTML);
            Path target = tempDir.resolve("report.pdf");

            pdfGenerationService.generatePdf(cohortProfile, report, narrative, target.toString());

            try (var files = Files.list(tempDir)) {
                assertThat(files).noneMatch(p -> p.getFileName().toString().endsWith(".pdf.tmp"));
            }
        }
    }

    @Nested
    @DisplayName("generatePdf() - failure branches")
    class FailureBranchTests {

        @Test
        @DisplayName("wraps failure to prepare the temp file/parent directory in PdfGenerationException")
        void parentDirectoryBlockedByExistingFile_throwsWrappedException() throws IOException {
            Path blocker = tempDir.resolve("blocker");
            Files.writeString(blocker, "not a directory");
            // tempDir/blocker/report.pdf is the target path, but blocker is a file, not a directory
            Path target = blocker.resolve("report.pdf");

            assertThatThrownBy(() ->
                    pdfGenerationService.generatePdf(cohortProfile, report, narrative, target.toString()))
                    .isInstanceOf(PdfGenerationException.class)
                    .hasMessageContaining("Failed to prepare temp file for PDF output")
                    .hasCauseInstanceOf(IOException.class);

            verify(pdfTemplateEngine, times(1)).process(eq("report"), any(Context.class));
        }

        @Test
        @DisplayName("wraps a render-stage IOException in PdfGenerationException and deletes the temp file")
        void renderIoExceptionIsWrapped() {
            when(pdfTemplateEngine.process(eq("report"), any(Context.class)))
                    .thenReturn(MINIMAL_VALID_XHTML);
            Path target = tempDir.resolve("report.pdf");

            // Mock construction of PdfRendererBuilder to throw an IOException on run()
            try (MockedConstruction<PdfRendererBuilder> mocked = mockConstruction(
                    PdfRendererBuilder.class,
                    (mockBuilder, context) -> doThrow(new IOException("simulated render failure"))
                            .when(mockBuilder).run())) {

                assertThatThrownBy(() ->
                        pdfGenerationService.generatePdf(cohortProfile, report, narrative, target.toString()))
                        .isInstanceOf(PdfGenerationException.class)
                        .hasMessageContaining("Failed to render PDF")
                        .hasCauseInstanceOf(IOException.class);
            }

            assertThat(target).doesNotExist();
            try (var files = Files.list(tempDir)) {
                assertThat(files).noneMatch(p -> p.getFileName().toString().endsWith(".pdf.tmp"));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Test
        @DisplayName("wraps failure to move the finished PDF into place in PdfGenerationException")
        void moveFailure_throwsWrappedException() throws IOException {
            when(pdfTemplateEngine.process(eq("report"), any(Context.class)))
                    .thenReturn(MINIMAL_VALID_XHTML);

            Path target = tempDir.resolve("report.pdf");
            Files.createDirectory(target);
            Files.writeString(target.resolve("occupant.txt"), "keeps the directory non-empty");

            assertThatThrownBy(() ->
                    pdfGenerationService.generatePdf(cohortProfile, report, narrative, target.toString()))
                    .isInstanceOf(PdfGenerationException.class)
                    .hasMessageContaining("Failed to move PDF into place")
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("wraps a real openhtmltopdf parse failure (unchecked exception) in PdfGenerationException")
        void renderFailureFromMalformedHtml_isWrapped() {
            String malformedHtml = "<!DOCTYPE html><html><body><p>Bad &middot; entity</p></body></html>";
            when(pdfTemplateEngine.process(eq("report"), any(Context.class)))
                    .thenReturn(malformedHtml);
            Path target = tempDir.resolve("report.pdf");

            assertThatThrownBy(() ->
                    pdfGenerationService.generatePdf(cohortProfile, report, narrative, target.toString()))
                    .isInstanceOf(PdfGenerationException.class)
                    .hasMessageContaining("Failed to render PDF");

            assertThat(target).doesNotExist();
        }
    }

    @Nested
    @DisplayName("deleteQuietly()")
    class DeleteQuietlyTests {

        @Test
        @DisplayName("swallows an IOException from deleteQuietly's cleanup attempt without masking the original failure")
        void deleteQuietlyFailure_isSwallowedAndDoesNotMaskOriginalException() throws IOException {
            when(pdfTemplateEngine.process(eq("report"), any(Context.class)))
                    .thenReturn(MINIMAL_VALID_XHTML);
            Path target = tempDir.resolve("report.pdf");

            try (MockedConstruction<PdfRendererBuilder> mockedBuilder = mockConstruction(
                    PdfRendererBuilder.class,
                    (mockBuilder, context) -> doThrow(new IOException("simulated render failure"))
                            .when(mockBuilder).run());
                 MockedStatic<Files> mockedFiles = mockStatic(Files.class, CALLS_REAL_METHODS)) {

                mockedFiles.when(() -> Files.deleteIfExists(any(Path.class)))
                        .thenThrow(new IOException("simulated delete failure"));

                assertThatThrownBy(() ->
                        pdfGenerationService.generatePdf(cohortProfile, report, narrative, target.toString()))
                        .isInstanceOf(PdfGenerationException.class)
                        .hasMessageContaining("Failed to render PDF");
            }

            // Proof deleteIfExists actually failed (rather than the mock silently not firing):
            // the .pdf.tmp temp file is still sitting in tempDir, since deletion never succeeded.
            try (var files = Files.list(tempDir)) {
                assertThat(files).anyMatch(p -> p.getFileName().toString().endsWith(".pdf.tmp"));
            }
        }

    }
}