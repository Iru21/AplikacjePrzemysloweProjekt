package me.iru.datingapp.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "User not found";

        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Resource not found";
        Throwable cause = new IllegalStateException("Database connection lost");

        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Database connection lost");
    }

    @Test
    void testConstructorWithResourceFieldAndValue() {
        String resourceName = "User";
        String fieldName = "id";
        Object fieldValue = 123L;

        ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("User not found with id: '123'");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testConstructorWithResourceFieldAndValue_StringValue() {
        String resourceName = "User";
        String fieldName = "email";
        String fieldValue = "test@example.com";

        ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

        assertThat(exception.getMessage()).isEqualTo("User not found with email: 'test@example.com'");
    }

    @Test
    void testConstructorWithResourceFieldAndValue_NullValue() {
        String resourceName = "Match";
        String fieldName = "id";
        Object fieldValue = null;

        ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

        assertThat(exception.getMessage()).isEqualTo("Match not found with id: 'null'");
    }

    @Test
    void testConstructorWithNullMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException(null);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThatThrownBy(() -> {
            throw new ResourceNotFoundException("Resource not found");
        })
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Resource not found");
    }

    @Test
    void testExceptionWithCauseCanBeThrown() {
        Exception cause = new RuntimeException("Database error");

        assertThatThrownBy(() -> {
            throw new ResourceNotFoundException("Not found", cause);
        })
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Not found")
                .hasCause(cause);
    }

    @Test
    void testExceptionWithFieldsCanBeThrown() {
        assertThatThrownBy(() -> {
            throw new ResourceNotFoundException("User", "id", 999L);
        })
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("id")
                .hasMessageContaining("999");
    }

    @Test
    void testExceptionInheritance() {
        ResourceNotFoundException exception = new ResourceNotFoundException("test");

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    void testResourceFieldConstructor_DifferentTypes() {
        ResourceNotFoundException ex1 = new ResourceNotFoundException("User", "id", 123L);
        assertThat(ex1.getMessage()).contains("123");

        ResourceNotFoundException ex2 = new ResourceNotFoundException("Match", "id", 456);
        assertThat(ex2.getMessage()).contains("456");

        ResourceNotFoundException ex3 = new ResourceNotFoundException("User", "email", "test@example.com");
        assertThat(ex3.getMessage()).contains("test@example.com");
    }

    @Test
    void testMessageFormatting_ResourceField() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Message", "id", 789L);

        assertThat(exception.getMessage()).matches(".+ not found with .+: '.+'");
        assertThat(exception.getMessage()).startsWith("Message not found with");
        assertThat(exception.getMessage()).endsWith("'789'");
    }

    @Test
    void testConstructorWithNullCause() {
        String message = "Not found";

        ResourceNotFoundException exception = new ResourceNotFoundException(message, null);

        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testLongMessage() {
        String longMessage = "Resource not found: " + "x".repeat(500);

        ResourceNotFoundException exception = new ResourceNotFoundException(longMessage);

        assertThat(exception.getMessage()).isEqualTo(longMessage);
    }

    @Test
    void testResourceFieldConstructor_ComplexResourceName() {
        String resourceName = "UserProfile";
        String fieldName = "userId";
        Long fieldValue = 12345L;

        ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

        assertThat(exception.getMessage()).isEqualTo("UserProfile not found with userId: '12345'");
    }

    @Test
    void testResourceFieldConstructor_BooleanValue() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Setting", "enabled", true);

        assertThat(exception.getMessage()).contains("true");
    }

    @Test
    void testMultipleCauseLevels() {
        Throwable rootCause = new IllegalStateException("Root cause");
        Throwable intermediateCause = new RuntimeException("Intermediate", rootCause);

        ResourceNotFoundException exception = new ResourceNotFoundException("Top level", intermediateCause);

        assertThat(exception.getCause()).isEqualTo(intermediateCause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }

    @Test
    void testCommonUseCases() {
        ResourceNotFoundException ex1 = new ResourceNotFoundException("User", "id", 1L);
        assertThat(ex1.getMessage()).isEqualTo("User not found with id: '1'");

        ResourceNotFoundException ex2 = new ResourceNotFoundException("Match", "id", 2L);
        assertThat(ex2.getMessage()).isEqualTo("Match not found with id: '2'");

        ResourceNotFoundException ex3 = new ResourceNotFoundException("Message", "id", 3L);
        assertThat(ex3.getMessage()).isEqualTo("Message not found with id: '3'");

        ResourceNotFoundException ex4 = new ResourceNotFoundException("User", "email", "test@example.com");
        assertThat(ex4.getMessage()).contains("test@example.com");
    }
}

