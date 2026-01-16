package com.hannos;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeLoader {

    private static final String LIBRARY_NAME = "pg_query";

    static {
        loadNativeLibrary();
    }

    private static void loadNativeLibrary() {
        final String os = System.getProperty("os.name").toLowerCase();

        final String extension;
        if (os.contains("win")) {
            extension = ".dll";
        } else if (os.contains("mac")) {
            extension = ".dylib";
        } else {
            extension = ".so";
        }
        final String libName = LIBRARY_NAME + extension;

        try (InputStream is = NativeLoader.class.getResourceAsStream("/" + libName)) {
            if (is == null) {
                throw new RuntimeException("Native library not found in resources: " + libName);
            }

            Path tempLib = Files.createTempFile("libpg_query", extension);
            tempLib.toFile().deleteOnExit();

            Files.copy(is, tempLib, StandardCopyOption.REPLACE_EXISTING);

            System.load(tempLib.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load native library", e);
        }
    }

    public static void ensureLoaded() {
        // No-op, just triggers static initializer
    }
}
