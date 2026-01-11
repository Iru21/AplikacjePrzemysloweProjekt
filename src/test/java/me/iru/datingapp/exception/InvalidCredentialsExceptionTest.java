package me.iru.datingapp.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class InvalidCredentialsExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Invalid email or password";

        InvalidCredentialsException exception = new InvalidCredentialsException(message);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Authentication failed";
        Throwable cause = new IllegalStateException("User locked");

        InvalidCredentialsException exception = new InvalidCredentialsException(message, cause);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("User locked");
    }

    @Test
    void testConstructorNoArgs() {
        InvalidCredentialsException exception = new InvalidCredentialsException();

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Invalid email or password");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testConstructorWithNullMessage() {
        InvalidCredentialsException exception = new InvalidCredentialsException(null);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThatThrownBy(() -> {
            throw new InvalidCredentialsException("Wrong password");
        })
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Wrong password");
    }

    @Test
    void testExceptionWithCauseCanBeThrown() {
        Exception cause = new RuntimeException("Password hash mismatch");

        assertThatThrownBy(() -> {
            throw new InvalidCredentialsException("Auth failed", cause);
        })
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Auth failed")
                .hasCause(cause);
    }

    @Test
    void testExceptionNoArgsCanBeThrown() {
        assertThatThrownBy(() -> {
            throw new InvalidCredentialsException();
        })
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void testExceptionInheritance() {
        InvalidCredentialsException exception = new InvalidCredentialsException();

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    void testDefaultMessage() {
        InvalidCredentialsException exception = new InvalidCredentialsException();

        assertThat(exception.getMessage()).isEqualTo("Invalid email or password");
        assertThat(exception.getMessage()).contains("email");
        assertThat(exception.getMessage()).contains("password");
    }

    @Test
    void testConstructorWithNullCause() {
        String message = "Invalid credentials";

        InvalidCredentialsException exception = new InvalidCredentialsException(message, null);

        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testMultipleCauseLevels() {
        Throwable rootCause = new IllegalStateException("Root cause");
        Throwable intermediateCause = new RuntimeException("Intermediate", rootCause);

        InvalidCredentialsException exception = new InvalidCredentialsException("Top level", intermediateCause);

        assertThat(exception.getCause()).isEqualTo(intermediateCause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }

    @Test
    void testCustomMessages() {
        String[] messages = {
                "Wrong password",
                "User not found",
                "Account locked",
                "Email does not exist",
                "Password expired"
        };

        for (String message : messages) {
            InvalidCredentialsException exception = new InvalidCredentialsException(message);
            assertThat(exception.getMessage()).isEqualTo(message);
        }
    }

    @Test
    void testLongMessage() {
        String longMessage = "Invalid credentials: " + "x".repeat(500);

        InvalidCredentialsException exception = new InvalidCredentialsException(longMessage);

        assertThat(exception.getMessage()).isEqualTo(longMessage);
    }

    @Test
    void testAllConstructorVariants() {
        InvalidCredentialsException ex1 = new InvalidCredentialsException("Custom message");
        assertThat(ex1.getMessage()).isEqualTo("Custom message");

        InvalidCredentialsException ex2 = new InvalidCredentialsException("Message", new RuntimeException("cause"));
        assertThat(ex2.getMessage()).isEqualTo("Message");
        assertThat(ex2.getCause()).isNotNull();

        InvalidCredentialsException ex3 = new InvalidCredentialsException();
        assertThat(ex3.getMessage()).isEqualTo("Invalid email or password");
    }

    @Test
    void testExceptionMessageWithSpecialCharacters() {
        String message = "Invalid: user@example.com failed authentication!";

        InvalidCredentialsException exception = new InvalidCredentialsException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testDefaultMessageFormat() {
        InvalidCredentialsException exception = new InvalidCredentialsException();

        assertThat(exception.getMessage()).matches("Invalid .+ or .+");
        assertThat(exception.getMessage()).doesNotContainIgnoringCase("null");
    }

    @Test
    void testExceptionInAuthenticationContext() {
        assertThatThrownBy(() -> {
            throw new InvalidCredentialsException();
        })
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid")
                .hasMessageContaining("email")
                .hasMessageContaining("password");
    }

    @Test
    void testCausePreservation() {
        RuntimeException originalCause = new RuntimeException("Database unavailable");

        InvalidCredentialsException exception = new InvalidCredentialsException("Cannot verify credentials", originalCause);

        assertThat(exception.getCause()).isSameAs(originalCause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Database unavailable");
    }
}

