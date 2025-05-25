package pl.edu.uj.tcs.aiplayground.dto;

public enum StatusType {
    QUEUE("Queue"),
    IN_PROGRESS("In Progress"),
    FINISHED("Finished"),
    ERROR("Error"),
    CANCELLED("Cancelled");

    String name;

    StatusType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
