package org.cris6h16.apirestspringboot.Utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
@Slf4j
public class FilesUtils {

    public void appendToFile(Path path, String content) {
        try {
            if (Files.notExists(path.getParent())) Files.createDirectories(path.getParent());

            // Write the content in the file
            try (BufferedWriter writer = Files.newBufferedWriter(
                    path,
                    StandardOpenOption.CREATE, // Create the file if not exists
                    StandardOpenOption.APPEND)
            ) {
                writer.write(content);
//                    writer.newLine(); // handle it manually is most intuitive
            }

        } catch (Exception e) {
            log.error("Error writing in file: {}", path, e);
        }
    }
}
