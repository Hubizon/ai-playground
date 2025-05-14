package pl.edu.uj.tcs.aiplayground.dto;

public enum StatusName {
    IN_PROGRESS("W Trakcie"),
    FINISHED("Ukończony"),
    ERROR("Niepowodzenie"),
    CANCELLED("Anulowany");

    String name;

    StatusName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
