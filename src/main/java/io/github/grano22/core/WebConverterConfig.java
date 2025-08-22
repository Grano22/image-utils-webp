package io.github.grano22.core;

public record WebConverterConfig(boolean debug) {
    public WebConverterConfig() {
        this(false);
    }

    public WebConverterConfig withDebug(boolean debug) {
        return new WebConverterConfig(debug);
    }
}
