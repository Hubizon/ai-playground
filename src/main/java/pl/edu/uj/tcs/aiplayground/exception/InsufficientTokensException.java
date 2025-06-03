package pl.edu.uj.tcs.aiplayground.exception;

public class InsufficientTokensException extends Exception {
    public InsufficientTokensException() {
        super();
    }

    public InsufficientTokensException(String message) {
        super(message);
    }

    public InsufficientTokensException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientTokensException(Throwable cause) {
        super(cause);
    }
}
