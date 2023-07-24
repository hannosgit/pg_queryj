package com.hannos;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class TestFileHelper {


    public static void runStatement(String filePath, Consumer<String> runnable) {
        final URL resource = SelectTests.class.getResource(filePath);

        try (final BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(resource.toURI()))) {
            StringBuilder sql = new StringBuilder();

            String line;
            do {
                try {
                    line = bufferedReader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                sql.append(line);
                if (line == null || line.contains(";")) {
                    runnable.accept(sql.toString());
                }
            } while (line != null);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
