package com.hannos;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeLoader {

    static {
        loadNativeLibrary();
    }

    private static void loadNativeLibrary() {
        String os = System.getProperty("os.name").toLowerCase();
        String libName;
        String extension;

        if (os.contains("win")) {
            libName = "pg_query.dll";
            extension = ".dll";
        } else if (os.contains("mac")) {
            libName = "pg_query.dylib";
            extension = ".dylib";
        } else {
            libName = "pg_query.so";
            extension = ".so";
        }

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
