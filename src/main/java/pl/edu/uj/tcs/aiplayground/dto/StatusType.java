package pl.edu.uj.tcs.aiplayground.dto;

public enum StatusType {
    QUEUE("Queue", false),
    IN_PROGRESS("In Progress", false),
    FINISHED("Finished", true),
    ERROR("Error", true),
    CANCELLED("Cancelled", true);

    String displayName;
    boolean isFinished;

    StatusType(String displayName, boolean isFinished) {
        this.displayName = displayName;
        this.isFinished = isFinished;
    }

    public String getName() {
        return displayName;
    }

    public boolean getIsFinished() {
        return isFinished;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
