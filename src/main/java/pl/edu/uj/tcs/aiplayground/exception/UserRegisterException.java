package pl.edu.uj.tcs.aiplayground.exception;

public class UserRegisterException extends Exception {
    public UserRegisterException() {
        super();
    }

    public UserRegisterException(String message) {
        super(message);
    }

    public UserRegisterException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserRegisterException(Throwable cause) {
        super(cause);
    }
}
