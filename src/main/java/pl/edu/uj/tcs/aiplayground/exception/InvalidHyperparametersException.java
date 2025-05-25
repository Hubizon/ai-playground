package pl.edu.uj.tcs.aiplayground.exception;

public class InvalidHyperparametersException extends Exception {
    public InvalidHyperparametersException() {
        super();
    }

    public InvalidHyperparametersException(String message) {
        super(message);
    }

    public InvalidHyperparametersException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidHyperparametersException(Throwable cause) {
        super(cause);
    }
}
