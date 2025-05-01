package org.example.aiplayground.exception;

public class EmailAlreadyUsedException extends Exception {
    public EmailAlreadyUsedException() {
        super();
    }

    public EmailAlreadyUsedException(String message) {
        super(message);
    }

    public EmailAlreadyUsedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailAlreadyUsedException(Throwable cause) {
        super(cause);
    }
}
