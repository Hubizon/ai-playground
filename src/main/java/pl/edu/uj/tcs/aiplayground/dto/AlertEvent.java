package pl.edu.uj.tcs.aiplayground.dto;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import java.util.Objects;
import java.util.UUID;

public record AlertEvent(
        String message,
        Boolean isInfo,
        UUID id) {
    public static AlertEvent createAlertEvent(String message, Boolean isInfo) {
        return new AlertEvent(message, isInfo, UUID.randomUUID());
    }

    public void display() {
        if (message != null && !message.isEmpty()) {
            Alert alert;
            if (isInfo)
                alert = new Alert(Alert.AlertType.INFORMATION);
            else
                alert = new Alert(Alert.AlertType.WARNING);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css")).toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("dialog-pane");
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(okButton);
            alert.showAndWait();
        }
    }
}
