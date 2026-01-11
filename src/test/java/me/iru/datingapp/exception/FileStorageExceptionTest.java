package me.iru.datingapp.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FileStorageExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Failed to store file";

        FileStorageException exception = new FileStorageException(message);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Failed to store file";
        Throwable cause = new IllegalArgumentException("Invalid file type");

        FileStorageException exception = new FileStorageException(message, cause);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause()).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("Invalid file type");
    }

    @Test
    void testConstructorWithNullMessage() {
        FileStorageException exception = new FileStorageException(null);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    void testConstructorWithNullCause() {
        String message = "File error";

        FileStorageException exception = new FileStorageException(message, null);

        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThatThrownBy(() -> {
            throw new FileStorageException("Test exception");
        })
        .isInstanceOf(FileStorageException.class)
        .hasMessage("Test exception");
    }

    @Test
    void testExceptionWithCauseCanBeThrown() {
        Exception cause = new RuntimeException("Original cause");

        assertThatThrownBy(() -> {
            throw new FileStorageException("Wrapped exception", cause);
        })
        .isInstanceOf(FileStorageException.class)
        .hasMessage("Wrapped exception")
        .hasCause(cause);
    }

    @Test
    void testExceptionInheritance() {
        FileStorageException exception = new FileStorageException("test");

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    void testMultipleCauseLevels() {
        Throwable rootCause = new IllegalStateException("Root cause");
        Throwable intermediateCause = new RuntimeException("Intermediate", rootCause);

        FileStorageException exception = new FileStorageException("Top level", intermediateCause);

        assertThat(exception.getCause()).isEqualTo(intermediateCause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }

    @Test
    void testExceptionMessage_LongMessage() {
        String longMessage = "Failed to store file: " + "x".repeat(1000);

        FileStorageException exception = new FileStorageException(longMessage);

        assertThat(exception.getMessage()).isEqualTo(longMessage);
        assertThat(exception.getMessage()).hasSize(longMessage.length());
    }

    @Test
    void testExceptionMessage_SpecialCharacters() {
        String message = "Failed: file@#$%^&*().jpg with path /var/tmp/uploads";

        FileStorageException exception = new FileStorageException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }
}

