package io.github.grano22.util;

public interface CommandLineBuilderFactory {
    public SimpleCommandRepresentation createParent();
    public SimpleCommandRepresentation create();
}
