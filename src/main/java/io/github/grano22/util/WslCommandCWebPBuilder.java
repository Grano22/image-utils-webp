package io.github.grano22.util;

import java.io.IOException;
import java.io.UncheckedIOException;

import static io.github.grano22.util.WslUtils.toWslPath;

public record WslCommandCWebPBuilder(String execPath) implements CommandLineBuilderFactory {
    @Override
    public SimpleCommandRepresentation createParent() {
        return new SimpleCommandRepresentation("wsl");
    }

    @Override
    public SimpleCommandRepresentation create() {
        try {
            return new SimpleCommandRepresentation("\"" + ((execPath == null || execPath.isBlank()) ? "cwebp" : toWslPath(execPath)) + "\"");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
