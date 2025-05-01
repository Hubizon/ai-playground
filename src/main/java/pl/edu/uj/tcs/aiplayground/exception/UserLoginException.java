package pl.edu.uj.tcs.aiplayground.exception;

public class UserLoginException extends Exception {
    public UserLoginException() {
        super();
    }

    public UserLoginException(String message) {
        super(message);
    }

    public UserLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserLoginException(Throwable cause) {
        super(cause);
    }
}
