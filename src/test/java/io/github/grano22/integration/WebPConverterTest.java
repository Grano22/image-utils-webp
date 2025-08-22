package io.github.grano22.integration;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;
import io.github.grano22.WebPConverter;
import io.github.grano22.core.WebConverterConfig;
import io.github.grano22.kit.TestEnvironment;
import io.github.grano22.util.WslCommandCWebPBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static io.github.grano22.util.WslUtils.toWslPath;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class WebPConverterTest {
    @BeforeAll
    public static void setup() {
        IIORegistry.getDefaultInstance().registerServiceProvider(new WebPImageReaderSpi());
    }

    @AfterEach
    public void cleanup() throws IOException {
        // Hint - comment this step when you need to physically investigate an image
        Path resultsPath = Path.of(TestEnvironment.getTestsPackageRootPath().toString(), "data", "result").toAbsolutePath();

        try (var walker = Files.walk(resultsPath)) {
            walker
                    .filter(p -> !p.equals(resultsPath))
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void webPConversionWasSuccessfulForFileBufferOnWindowsAndWsl() throws IOException {
        // Arrange
        Path realFsSourceFilePath = Path.of(TestEnvironment.getTestsPackageRootPath().toString(), "data", "images", "avocado.png").toAbsolutePath();
        Path realFsTargetFilePath = Path.of(TestEnvironment.getTestsPackageRootPath().toString(), "data", "result/avocado.webp").toAbsolutePath();
        Files.createDirectories(realFsTargetFilePath.getParent());

        String cWebPLinuxBinaryPath = Path.of(TestEnvironment.getTestsPackageRootPath().toString(), "data", "bin", "cwebp").toAbsolutePath().toString();
        var converter = new WebPConverter(new WebConverterConfig(), new WslCommandCWebPBuilder(cWebPLinuxBinaryPath));

        // Act
        converter.convert(toWslPath(realFsSourceFilePath.toString()), toWslPath(realFsTargetFilePath.toString()));

        // Assert
        byte[] webPImageBytes = Files.readAllBytes(realFsTargetFilePath);
        assert webPImageBytes.length > 0;
        assertItIsCorrectWebPImage(webPImageBytes, 64, 64);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void webPConversionWasSuccessfulForInMemoryBufferOnWindowsAndWsl() throws IOException {
        // Arrange
        String cWebPLinuxBinaryPath = Path.of(TestEnvironment.getTestsPackageRootPath().toString(), "data", "bin", "cwebp").toAbsolutePath().toString();
        String targetImage = Path.of(TestEnvironment.getTestsPackageRootPath().toString(), "data", "images", "avocado.png").toAbsolutePath().toString();
        byte[] targetImageBytes = Files.readAllBytes(Path.of(targetImage));
        var converter = new WebPConverter(new WebConverterConfig(), new WslCommandCWebPBuilder(cWebPLinuxBinaryPath));

        // Act
        byte[] webPImage = converter.convert(targetImageBytes);

        // Assert
        assert webPImage != null;
        assert webPImage.length > 0;
        assertItIsCorrectWebPImage(webPImage, 64, 64);
    }

    private void assertItIsCorrectWebPImage(byte[] webpBytes, int requiredWidth, int requiredHeight) {
        var errors = checkWebpImageForErrors(webpBytes, requiredWidth, requiredHeight);

        assertArrayEquals(
                new String[0],
                errors.toArray(new String[0]),
                Arrays.toString(errors.toArray())
        );
    }

    private ArrayList<String> checkWebpImageForErrors(byte[] webpBytes, int requiredWidth, int requiredHeight) {
        ArrayList<String> errors = new ArrayList<>();

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(webpBytes);
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
            ImageReader webPReader = ImageIO.getImageReadersByFormatName("webp").next();
            webPReader.setInput(imageInputStream);

            BufferedImage img = webPReader.read(0);

            if (img == null) {
                errors.add("WebP Image reader returned null");

                return errors;
            }

            if (img.getWidth() != requiredWidth) {
                errors.add("Image width is " + img.getWidth() + ", but " + requiredWidth + " was expected");
            }

            if (img.getHeight() != requiredHeight) {
                errors.add("Image height is " + img.getHeight() + ", but " + requiredHeight + " was expected");
            }
        } catch (Exception e) {
            errors.add("Reading of WebP bytes failed: " + e.getMessage());
        }

        return errors;
    }
}
