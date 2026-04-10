package com.ims;

import com.ims.controller.MainController;
import com.ims.db.DatabaseConnection;
import com.ims.db.DatabaseInitializer;
import com.ims.model.User;
import com.ims.service.AuthService;
import com.ims.util.AlertUtil;
import com.ims.util.DatabaseSetupDialog;
import com.ims.view.LoginView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;

public class App extends Application {
    private static final String APP_TITLE = "Inventory Management System";
    private final AuthService authService = new AuthService();
    private Scene scene;

    @Override
    public void start(Stage stage) {
        if (!ensureDatabaseReady()) {
            Platform.exit();
            return;
        }

        LoginView loginView = createLoginView(stage);
        scene = new Scene(loginView.getRoot(), 1180, 740);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/dashboard.png"))));
        stage.setTitle(APP_TITLE + " | Sign In");
        stage.setMinWidth(1040);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private boolean ensureDatabaseReady() {
        while (true) {
            try {
                DatabaseInitializer.initialize();
                return true;
            } catch (Exception exception) {
                DatabaseConnection connection = DatabaseConnection.getInstance();
                Optional<DatabaseConnection.Settings> updatedSettings = DatabaseSetupDialog.show(
                    connection.getSettings(),
                    exception,
                    connection.getConfigLocation()
                );
                if (updatedSettings.isEmpty()) {
                    AlertUtil.error("Database Connection Failed", friendlyStartupMessage(exception));
                    return false;
                }
                DatabaseConnection.saveSettings(updatedSettings.get());
                DatabaseConnection.reset();
            }
        }
    }

    private LoginView createLoginView(Stage stage) {
        LoginView loginView = new LoginView();
        loginView.getLoginButton().setOnAction(event -> attemptLogin(stage, loginView));
        return loginView;
    }

    private void attemptLogin(Stage stage, LoginView loginView) {
        try {
            User user = authService.login(loginView.getUsername(), loginView.getPassword(), loginView.getSelectedRole());
            MainController controller = new MainController(user, () -> showLogin(stage));
            scene.setRoot(controller.getView());
            stage.setTitle(APP_TITLE + " | " + user.getRole() + " Workspace - " + user.getUsername());
        } catch (IllegalArgumentException exception) {
            loginView.showStatus(exception.getMessage(), true);
        } catch (Exception exception) {
            AlertUtil.error("Login Failed", friendlyMessage(exception));
        }
    }

    private void showLogin(Stage stage) {
        LoginView loginView = createLoginView(stage);
        scene.setRoot(loginView.getRoot());
        stage.setTitle(APP_TITLE + " | Sign In");
    }

    private String friendlyMessage(Exception exception) {
        return exception.getMessage() == null ? "Something went wrong. Please try again." : exception.getMessage();
    }

    private String friendlyStartupMessage(Exception exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        String details = rootCause.getMessage() == null ? exception.getMessage() : rootCause.getMessage();
        return "Check your MySQL settings in " + DatabaseConnection.getInstance().getConfigLocation()
            + System.lineSeparator() + System.lineSeparator()
            + "Required keys:"
            + System.lineSeparator() + "ims.db.host=localhost"
            + System.lineSeparator() + "ims.db.port=3306"
            + System.lineSeparator() + "ims.db.name=ims"
            + System.lineSeparator() + "ims.db.user=root"
            + System.lineSeparator() + "ims.db.password=your_mysql_password"
            + System.lineSeparator() + System.lineSeparator()
            + "MySQL said: " + (details == null ? "Unknown database error." : details);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
