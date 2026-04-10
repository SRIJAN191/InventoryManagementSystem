package com.ims.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public final class AlertUtil {
    private AlertUtil() {
    }

    public static void info(String title, String message) {
        show(AlertType.INFORMATION, title, message);
    }

    public static void error(String title, String message) {
        show(AlertType.ERROR, title, message);
    }

    public static void warning(String title, String message) {
        show(AlertType.WARNING, title, message);
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private static void show(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
