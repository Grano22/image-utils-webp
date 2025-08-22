package io.github.grano22.util;

public record DefaultCommandCWebPBuilder(String execPath, SimpleCommandRepresentation parent) implements CommandLineBuilderFactory {
    public DefaultCommandCWebPBuilder(String execPath) {
        this(execPath, null);
    }

    @Override
    public SimpleCommandRepresentation createParent() {
        return parent;
    }

    @Override
    public SimpleCommandRepresentation create() {
        var finalBinaryPath = execPath;

        if (finalBinaryPath == null || finalBinaryPath.isBlank()) {
            finalBinaryPath = "cwebp";
        }

        return new SimpleCommandRepresentation(finalBinaryPath);
    }
}
