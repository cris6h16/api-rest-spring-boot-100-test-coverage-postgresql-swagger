package org.cris6h16.apirestspringboot.Utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilesUtilsTest {

    @Autowired
    private FilesUtils filesUtils;

    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("test", ".txt");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void appendToFile_existentFile() throws IOException {
        String content = "cris6h16's exception";
        filesUtils.appendToFile(tempFile, content);

        String fileContent = Files.readString(tempFile);
        assertEquals(content, fileContent);
    }

    @Test
    void appendToFile_notExistentFile() throws IOException {
        // Arrange
        String content = "cris6h16's exception";
        Files.deleteIfExists(tempFile);

        // Act
        filesUtils.appendToFile(tempFile, content);

        // Assert
        String fileContent = Files.readString(tempFile);
        assertEquals(content, fileContent);
    }

    @Test
    void appendToFile_appendWithExistentContent() throws IOException {
        // Arrange
        String content = "cris6h16's exception";
        filesUtils.appendToFile(tempFile, content);
        assertEquals(content, Files.readString(tempFile));

        String newContent = "cris6h16's exception 2";

        // Act
        filesUtils.appendToFile(tempFile, newContent);

        // Assert
        String fileContent = Files.readString(tempFile);
        assertEquals(content + newContent, fileContent);
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "", "  "})
    void appendToFile_pathNullOrEmptyOrBlank_ThenIllegalArgumentException(String path) {
        // Arrange
        path = path.equals("null") ? null : path;
        String content = "cris6h16's exception";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> filesUtils.appendToFile(null, content));
    }

    @Test
    void appendToFile_contentNull_ShouldWriteEmptyString() throws IOException {
        // Arrange
        String content = null;

        // Act
        filesUtils.appendToFile(tempFile, content);

        // Assert
        String fileContent = Files.readString(tempFile);
        assertEquals("", fileContent);
    }

    @Test
    void appendToFile_UnexpectedException_ShouldThrowRuntimeExceptionContainingTheExceptionToString() {
        // Arrange
        Path path = tempFile.resolve("not-exists-dir").resolve("file.txt"); // .../tempFile.txt/not-exists-dir/file.txt --> invalid path
        String content = "cris6h16's exception";
        Exception exception = null;

        try {
            Files.newBufferedWriter(
                    path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (Exception e) {
             exception = e;
        }

        // Act & Assert
        assertThatThrownBy(() -> filesUtils.appendToFile(path, content))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(exception.toString()); // Check if the exception.toString() is in the message of the runtime exception
    }


}
