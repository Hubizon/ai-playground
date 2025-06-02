package pl.edu.uj.tcs.aiplayground.dto;

public enum StatusType {
    QUEUE("Queue", false),
    IN_PROGRESS("In Progress", false),
    FINISHED("Finished", true),
    ERROR("Error", true),
    CANCELLED("Cancelled", true);

    String name;
    boolean isFinished;

    StatusType(String name, boolean isFinished) {
        this.name = name;
        this.isFinished = isFinished;
    }

    public String getName() {
        return name;
    }

    public boolean getIsFinished() { return isFinished; }
}
