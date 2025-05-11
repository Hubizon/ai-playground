package pl.edu.uj.tcs.aiplayground.exception;

public class UserModificationException extends Exception {
    public UserModificationException() {
        super();
    }

    public UserModificationException(String message) {
        super(message);
    }

    public UserModificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserModificationException(Throwable cause) {
        super(cause);
    }
}
