package pl.edu.uj.tcs.aiplayground.exception;

public class ModelModificationException extends Exception {
    public ModelModificationException() {
        super();
    }

    public ModelModificationException(String message) {
        super(message);
    }

    public ModelModificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelModificationException(Throwable cause) {
        super(cause);
    }
}
