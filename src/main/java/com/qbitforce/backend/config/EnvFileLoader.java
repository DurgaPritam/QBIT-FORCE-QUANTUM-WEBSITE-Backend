package com.qbitforce.backend.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads backend/.env when the app is started from the repo root (e.g. IDE Run). */
public final class EnvFileLoader {

    private static final Logger log = LoggerFactory.getLogger(EnvFileLoader.class);

    private EnvFileLoader() {}

    public static void load() {
        for (Path path : List.of(Path.of("backend", ".env"), Path.of(".env"))) {
            if (!Files.isRegularFile(path)) {
                continue;
            }
            try {
                for (String line : Files.readAllLines(path)) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    int eq = trimmed.indexOf('=');
                    if (eq <= 0) {
                        continue;
                    }
                    String key = trimmed.substring(0, eq).trim();
                    String value = trimmed.substring(eq + 1).trim();
                    if (System.getenv(key) == null && System.getProperty(key) == null) {
                        System.setProperty(key, value);
                    }
                }
                log.info("Loaded environment from {}", path.toAbsolutePath().normalize());
                return;
            } catch (IOException ex) {
                log.warn("Could not read {}", path, ex);
            }
        }
    }
}
