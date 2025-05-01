package org.example.aiplayground.exception;

public class InvalidLoginOrPasswordException extends Exception {
    public InvalidLoginOrPasswordException() {
        super();
    }

    public InvalidLoginOrPasswordException(String message) {
        super(message);
    }

    public InvalidLoginOrPasswordException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidLoginOrPasswordException(Throwable cause) {
        super(cause);
    }
}
