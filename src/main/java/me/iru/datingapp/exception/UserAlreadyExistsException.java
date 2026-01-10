package me.iru.datingapp.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String email) {
        super(String.format("User with email '%s' already exists", email));
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

