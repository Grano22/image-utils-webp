package io.github.grano22.kit;

import java.nio.file.Path;

public class TestEnvironment {
    private static Path cachedTestsRootPath;
    private static Path cachedTestTypesRootPath;

    public static Path getTestTypesRootPath(Class<?> testClass) {
        if (cachedTestTypesRootPath != null) {
            return cachedTestTypesRootPath;
        }

        String packageName = testClass.getPackage().getName();
        String[] parts = packageName.split("\\.");

        return cachedTestTypesRootPath = Path.of(getTestsRootPath().toString(), parts[parts.length - 1]);
    }

    public static Path getTestsPackageRootPath() {
        if (cachedTestsRootPath != null) {
            return cachedTestsRootPath;
        }

        String packageName = TestEnvironment.class.getPackage().getName().replace(".kit", "");

        return cachedTestsRootPath = Path.of(getTestsRootPath().toString(), packageName.replace(".", "/"));
    }

    public static Path getTestsRootPath() {
        return Path.of(System.getProperty("user.dir"), "src", "test", "java");
    }
}
