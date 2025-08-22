package io.github.grano22;

import io.github.grano22.core.WebConverterConfig;
import io.github.grano22.util.CommandLineBuilderFactory;
import io.github.grano22.core.ConverionToWebPFailed;
import io.github.grano22.util.SimpleCommandRepresentation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class WebPConverter {
    private final Logger logger = Logger.getLogger(WebPConverter.class.getName());
    private final WebConverterConfig config;

    private SimpleCommandRepresentation finalCommandBuilder;
    private SimpleCommandRepresentation cWebPCommandBuilder;

    public WebPConverter(
        WebConverterConfig config,
        CommandLineBuilderFactory commandLineBuilderFactory
    ) {
        this.config = config;

        cWebPCommandBuilder = commandLineBuilderFactory.create();
        var parentCommand = commandLineBuilderFactory.createParent();
        finalCommandBuilder = parentCommand != null ? parentCommand.clone() : cWebPCommandBuilder;
    }

    public void convert(String sourcePath, String outputPath) {
        try {
            var finalStepDWebPCommandBuilder = cWebPCommandBuilder.clone();
            var finalStepCommandBuilder = finalCommandBuilder.clone();

            finalStepDWebPCommandBuilder
                .setOption("-o", outputPath)
                .useOptionsTerminator()
                .addArgument(sourcePath)
            ;
            finalStepCommandBuilder.addArgument(finalStepDWebPCommandBuilder);

            var finalCommandLineParts = finalStepCommandBuilder.buildAsCommandLineParts();

            if (config.debug()) {
                logger.info("Final command line: " + finalCommandLineParts);
            }

            ProcessBuilder pb = new ProcessBuilder(finalCommandLineParts);
            pb.redirectErrorStream(false);

            Process process = pb.start();

            process.waitFor();

            if (process.exitValue() != 0) {
                String errors = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

                throw new IllegalStateException("WebP process failed: " + errors);
            }
        } catch (IllegalStateException | IOException | InterruptedException e) {
            logger.warning("WebP conversion failed\n" + e.getMessage());

            throw new ConverionToWebPFailed("cwebp command failed", e);
        }
    }

    public byte[] convert(byte[] imageBytes) {
        try {
            var finalStepDWebPCommandBuilder = cWebPCommandBuilder.clone();
            var finalStepCommandBuilder = finalCommandBuilder.clone();

            finalStepDWebPCommandBuilder
                    .setOption("-o", "-")
                    .useOptionsTerminator()
                    .addArgument("-")
            ;
            finalStepCommandBuilder.addArgument(finalStepDWebPCommandBuilder);

            var finalCommandLineParts = finalStepCommandBuilder.buildAsCommandLineParts();

            if (config.debug()) {
                logger.info("Final command line: " + finalCommandLineParts);
            }

            ProcessBuilder pb = new ProcessBuilder(finalCommandLineParts);
            pb.redirectErrorStream(false);

            Process process = pb.start();

            try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
                ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
                ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

                Future<?> stdinFlow = executor.submit(() -> {
                    try (OutputStream os = process.getOutputStream()) {
                        os.write(imageBytes);
                        os.flush();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

                Future<?> stdoutFlow = executor.submit(() -> {
                    try (InputStream is = process.getInputStream()) {
                        is.transferTo(stdOut);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

                Future<?> stderrFlow = executor.submit(() -> {
                    try (InputStream es = process.getErrorStream()) {
                        es.transferTo(stdErr);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

                stdinFlow.get();
                stdoutFlow.get();
                stderrFlow.get();

                process.waitFor();

                if (process.exitValue() != 0) {
                    throw new IllegalStateException("WebP process failed: " + stdErr.toString(StandardCharsets.UTF_8));
                }

                return stdOut.toByteArray();
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.warning("WebP conversion failed\n" + e.getMessage());

            throw new ConverionToWebPFailed("cwebp command failed", e);
        }
    }

    public WebPConverter quality(int quality) {
        cWebPCommandBuilder.setOption("-q", String.valueOf(quality));

        return this;
    }

    public WebPConverter crop(int x_position, int y_position, int width, int height) {
        if (width == 0 || height == 0) {
            cWebPCommandBuilder.removeOption("-crop");
        }

        cWebPCommandBuilder.setOption("-crop", x_position + " " + y_position + " " + width + " " + height);

        return this;
    }
}