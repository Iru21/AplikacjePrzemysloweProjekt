package me.iru.datingapp.exception;

public class MatchNotActiveException extends RuntimeException {

    public MatchNotActiveException(String message) {
        super(message);
    }

    public MatchNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public MatchNotActiveException(Long matchId) {
        super(String.format("Match with ID %d is not active", matchId));
    }

    public MatchNotActiveException() {
        super("Cannot perform this operation on an inactive match");
    }
}

