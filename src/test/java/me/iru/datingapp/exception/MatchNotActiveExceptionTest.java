package me.iru.datingapp.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MatchNotActiveExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Match is not active";

        MatchNotActiveException exception = new MatchNotActiveException(message);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Cannot send message to inactive match";
        Throwable cause = new IllegalStateException("Match deactivated");

        MatchNotActiveException exception = new MatchNotActiveException(message, cause);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Match deactivated");
    }

    @Test
    void testConstructorWithMatchId() {
        Long matchId = 123L;

        MatchNotActiveException exception = new MatchNotActiveException(matchId);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Match with ID 123 is not active");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testConstructorNoArgs() {
        MatchNotActiveException exception = new MatchNotActiveException();

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Cannot perform this operation on an inactive match");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testConstructorWithNullMessage() {
        MatchNotActiveException exception = new MatchNotActiveException((String) null);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    void testConstructorWithNullMatchId() {
        MatchNotActiveException exception = new MatchNotActiveException((Long) null);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Match with ID null is not active");
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThatThrownBy(() -> {
            throw new MatchNotActiveException("Match inactive");
        })
                .isInstanceOf(MatchNotActiveException.class)
                .hasMessage("Match inactive");
    }

    @Test
    void testExceptionWithCauseCanBeThrown() {
        Exception cause = new RuntimeException("Original error");

        assertThatThrownBy(() -> {
            throw new MatchNotActiveException("Match error", cause);
        })
                .isInstanceOf(MatchNotActiveException.class)
                .hasMessage("Match error")
                .hasCause(cause);
    }

    @Test
    void testExceptionWithMatchIdCanBeThrown() {
        assertThatThrownBy(() -> {
            throw new MatchNotActiveException(999L);
        })
                .isInstanceOf(MatchNotActiveException.class)
                .hasMessageContaining("999")
                .hasMessageContaining("not active");
    }

    @Test
    void testExceptionNoArgsCanBeThrown() {
        assertThatThrownBy(() -> {
            throw new MatchNotActiveException();
        })
                .isInstanceOf(MatchNotActiveException.class)
                .hasMessage("Cannot perform this operation on an inactive match");
    }

    @Test
    void testExceptionInheritance() {
        MatchNotActiveException exception = new MatchNotActiveException();

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    void testMatchIdFormatting_DifferentIds() {
        Long[] ids = {1L, 100L, 999L, 12345L};

        for (Long id : ids) {
            MatchNotActiveException exception = new MatchNotActiveException(id);
            assertThat(exception.getMessage()).isEqualTo("Match with ID " + id + " is not active");
            assertThat(exception.getMessage()).contains(id.toString());
        }
    }

    @Test
    void testConstructorWithNullCause() {
        String message = "Match not active";

        MatchNotActiveException exception = new MatchNotActiveException(message, null);

        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testDefaultMessage() {
        MatchNotActiveException exception = new MatchNotActiveException();

        assertThat(exception.getMessage()).isEqualTo("Cannot perform this operation on an inactive match");
        assertThat(exception.getMessage()).contains("inactive");
        assertThat(exception.getMessage()).contains("operation");
    }

    @Test
    void testMatchIdMessage_ContainsIdAndStatus() {
        Long matchId = 42L;

        MatchNotActiveException exception = new MatchNotActiveException(matchId);

        assertThat(exception.getMessage()).contains("Match with ID");
        assertThat(exception.getMessage()).contains("42");
        assertThat(exception.getMessage()).contains("not active");
    }

    @Test
    void testMultipleCauseLevels() {
        Throwable rootCause = new IllegalStateException("Root cause");
        Throwable intermediateCause = new RuntimeException("Intermediate", rootCause);

        MatchNotActiveException exception = new MatchNotActiveException("Top level", intermediateCause);

        assertThat(exception.getCause()).isEqualTo(intermediateCause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }

    @Test
    void testLongCustomMessage() {
        String longMessage = "Match is not active: " + "x".repeat(500);

        MatchNotActiveException exception = new MatchNotActiveException(longMessage);

        assertThat(exception.getMessage()).isEqualTo(longMessage);
    }

    @Test
    void testAllConstructorVariants() {
        MatchNotActiveException ex1 = new MatchNotActiveException("Custom message");
        assertThat(ex1.getMessage()).isEqualTo("Custom message");

        MatchNotActiveException ex2 = new MatchNotActiveException("Message", new RuntimeException("cause"));
        assertThat(ex2.getMessage()).isEqualTo("Message");
        assertThat(ex2.getCause()).isNotNull();

        MatchNotActiveException ex3 = new MatchNotActiveException(123L);
        assertThat(ex3.getMessage()).contains("123");

        MatchNotActiveException ex4 = new MatchNotActiveException();
        assertThat(ex4.getMessage()).isEqualTo("Cannot perform this operation on an inactive match");
    }

    @Test
    void testMatchIdZero() {
        MatchNotActiveException exception = new MatchNotActiveException(0L);

        assertThat(exception.getMessage()).isEqualTo("Match with ID 0 is not active");
    }

    @Test
    void testMatchIdNegative() {
        MatchNotActiveException exception = new MatchNotActiveException(-1L);

        assertThat(exception.getMessage()).isEqualTo("Match with ID -1 is not active");
    }
}

