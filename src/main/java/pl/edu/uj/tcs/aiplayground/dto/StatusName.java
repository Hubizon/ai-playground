package pl.edu.uj.tcs.aiplayground.dto;

public enum StatusName {
    QUEUE("Queue"),
    IN_PROGRESS("In Progress"),
    FINISHED("Finished"),
    ERROR("Error"),
    CANCELLED("Cancelled");

    String name;

    StatusName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
