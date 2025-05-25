package pl.edu.uj.tcs.aiplayground.exception;

public class TrainingException extends Exception {
    public TrainingException() {
        super();
    }

    public TrainingException(String message) {
        super(message);
    }

    public TrainingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrainingException(Throwable cause) {
        super(cause);
    }
}
