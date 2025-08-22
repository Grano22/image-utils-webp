package io.github.grano22.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WslUtils {
    public static String toWslPath(String windowsPath) throws IOException {
        Process process = new ProcessBuilder("wsl", "wslpath", "\"" + windowsPath + "\"")
                .redirectErrorStream(true)
                .start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String wslPath = reader.readLine();
            if (wslPath != null) {
                return wslPath.trim();
            }

            throw new RuntimeException("wsl wslpath did not return output");
        }
    }
}
