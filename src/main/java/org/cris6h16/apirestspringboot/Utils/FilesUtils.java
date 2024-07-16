package org.cris6h16.apirestspringboot.Utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Utility class for files.
 * <p>
 * This class contains methods to work with files.
 **/
@Component
@Slf4j
public class FilesUtils {

    /**
     * Write content in a file.
     * <p>
     * you should use this method into a synchronized block properly
     * locked, because this method due to optimization reasons is not
     * thread-safe.
     * </p>
     *
     * @param path    The path of the file ( relative to the project root recommended ).
     * @param content The content to write in the file.
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    public void appendToFile(Path path, String content) {
        if (path == null) throw new IllegalArgumentException("The path can't be null");
        if (content == null) content = "";

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
            log.error("Error writing in file, path: {}, exception: {}", path, e.toString());
            throw new RuntimeException("Error writing in file, path: " + path + ", exception: " + e.toString());
        }
    }
}
