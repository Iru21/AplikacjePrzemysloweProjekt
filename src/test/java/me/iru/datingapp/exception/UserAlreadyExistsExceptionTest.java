package me.iru.datingapp.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserAlreadyExistsExceptionTest {

    @Test
    void testConstructorWithEmail() {
        String email = "test@example.com";

        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("User with email 'test@example.com' already exists");
        assertThat(exception.getCause()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Custom error message";
        Throwable cause = new RuntimeException("Database error");

        UserAlreadyExistsException exception = new UserAlreadyExistsException(message, cause);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Database error");
    }

    @Test
    void testConstructorWithNullEmail() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException(null);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("User with email 'null' already exists");
    }

    @Test
    void testConstructorFormatsEmailCorrectly() {
        String email = "john.doe@company.org";

        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        assertThat(exception.getMessage()).contains(email);
        assertThat(exception.getMessage()).startsWith("User with email '");
        assertThat(exception.getMessage()).endsWith("' already exists");
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThatThrownBy(() -> {
            throw new UserAlreadyExistsException("duplicate@example.com");
        })
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("duplicate@example.com");
    }

    @Test
    void testExceptionWithCauseCanBeThrown() {
        Exception cause = new IllegalStateException("Unique constraint violated");

        assertThatThrownBy(() -> {
            throw new UserAlreadyExistsException("User exists", cause);
        })
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User exists")
                .hasCause(cause);
    }

    @Test
    void testExceptionInheritance() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("test@example.com");

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    void testEmailWithSpecialCharacters() {
        String email = "user+test@sub-domain.co.uk";

        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        assertThat(exception.getMessage()).contains(email);
        assertThat(exception.getMessage()).isEqualTo("User with email 'user+test@sub-domain.co.uk' already exists");
    }

    @Test
    void testLongEmailAddress() {
        String email = "very.long.email.address.with.many.dots@very-long-domain-name.com";

        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        assertThat(exception.getMessage()).contains(email);
    }

    @Test
    void testEmptyEmail() {
        String email = "";

        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        assertThat(exception.getMessage()).isEqualTo("User with email '' already exists");
    }

    @Test
    void testConstructorWithNullCause() {
        String message = "User exists";

        UserAlreadyExistsException exception = new UserAlreadyExistsException(message, null);

        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testMessageFormatting_MultipleEmails() {
        String[] emails = {
                "test1@example.com",
                "test2@example.com",
                "test3@example.com"
        };

        for (String email : emails) {
            UserAlreadyExistsException exception = new UserAlreadyExistsException(email);
            assertThat(exception.getMessage()).matches("User with email '.+' already exists");
        }
    }
}

