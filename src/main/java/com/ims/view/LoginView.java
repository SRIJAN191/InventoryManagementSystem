package com.ims.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class LoginView {
    private final StackPane root = new StackPane();
    private final ToggleButton adminRoleButton = new ToggleButton("Admin");
    private final ToggleButton staffRoleButton = new ToggleButton("Staff");
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Button loginButton = new Button("Sign In to Workspace");
    private final Label statusLabel = new Label("Select your role and enter your credentials to continue.");

    public LoginView() {
        root.getStyleClass().add("login-root");

        Region orbOne = new Region();
        orbOne.getStyleClass().add("login-orb-one");
        Region orbTwo = new Region();
        orbTwo.getStyleClass().add("login-orb-two");
        Region orbThree = new Region();
        orbThree.getStyleClass().add("login-orb-three");

        Label brandEyebrow = new Label("SMART INVENTORY WORKSPACE");
        brandEyebrow.getStyleClass().add("brand-eyebrow");

        Label brandTitle = new Label("Professional inventory operations for Admin and Staff teams.");
        brandTitle.getStyleClass().add("brand-title");
        brandTitle.setWrapText(true);

        Label brandCopy = new Label(
            "Manage products, categories, stock movement, and sales from one focused workspace built for day-to-day business operations."
        );
        brandCopy.getStyleClass().add("brand-copy");
        brandCopy.setWrapText(true);

        HBox heroStats = new HBox(14,
            statCard("24/7", "Live access"),
            statCard("ADMIN", "Role control"),
            statCard("NPR", "Localized pricing")
        );
        heroStats.getStyleClass().add("hero-stat-grid");

        HBox showcase = new HBox(14,
            showcaseCard("/images/dashboard.png", "Dashboard"),
            showcaseCard("/images/product.png", "Products"),
            showcaseCard("/images/report.png", "Reports")
        );
        showcase.getStyleClass().add("showcase-strip");

        VBox highlights = new VBox(12,
            featureLine("1", "Secure role-based sign-in before dashboard access"),
            featureLine("2", "Structured product and category workflows for cleaner data"),
            featureLine("3", "Business KPIs, sales reporting, and operational activity tracking")
        );
        highlights.getStyleClass().add("brand-highlights");

        HBox securityStrip = new HBox(10,
            pill("ADMIN & STAFF"),
            pill("FAST DEMO ACCESS"),
            pill("INVENTORY READY")
        );
        securityStrip.getStyleClass().add("security-strip");

        VBox brandPanel = new VBox(22, brandEyebrow, brandTitle, brandCopy, heroStats, showcase, highlights, securityStrip);
        brandPanel.getStyleClass().add("brand-panel");
        brandPanel.setMaxWidth(620);

        ToggleGroup roleGroup = new ToggleGroup();
        adminRoleButton.setToggleGroup(roleGroup);
        staffRoleButton.setToggleGroup(roleGroup);
        adminRoleButton.getStyleClass().add("role-toggle");
        staffRoleButton.getStyleClass().add("role-toggle");
        adminRoleButton.setSelected(true);
        adminRoleButton.setMaxWidth(Double.MAX_VALUE);
        staffRoleButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(adminRoleButton, Priority.ALWAYS);
        HBox.setHgrow(staffRoleButton, Priority.ALWAYS);

        HBox rolePicker = new HBox(12, adminRoleButton, staffRoleButton);
        rolePicker.getStyleClass().add("role-picker");

        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("auth-field");
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("auth-field");
        loginButton.getStyleClass().addAll("primary-action", "login-button");
        loginButton.setGraphic(iconView("/images/dashboard.png", 16));
        loginButton.setContentDisplay(ContentDisplay.LEFT);
        loginButton.setGraphicTextGap(10);
        loginButton.setDefaultButton(true);
        statusLabel.getStyleClass().addAll("login-status", "status-neutral");
        statusLabel.setWrapText(true);

        Label loginBadge = new Label("WELCOME BACK");
        loginBadge.getStyleClass().add("login-badge");

        Label miniNote = new Label("Admin users can manage categories, staff, and reports. Staff users can focus on sales and inventory operations.");
        miniNote.getStyleClass().add("mini-note");
        miniNote.setWrapText(true);

        VBox loginCard = new VBox(14,
            loginBadge,
            sectionTitle("Sign in to continue"),
            sectionCopy("Choose the appropriate role before signing in to load the correct workspace permissions and management tools."),
            rolePicker,
            fieldGroup("Username", usernameField),
            fieldGroup("Password", passwordField),
            loginButton,
            statusLabel,
            miniNote
        );
        loginCard.getStyleClass().add("login-card");
        loginCard.setPrefWidth(450);

        HBox layout = new HBox(42, brandPanel, loginCard);
        layout.getStyleClass().add("login-stage");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(28));

        root.getChildren().addAll(orbOne, orbTwo, orbThree, layout);
        StackPane.setAlignment(orbOne, Pos.TOP_LEFT);
        StackPane.setAlignment(orbTwo, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(orbThree, Pos.CENTER_RIGHT);
        StackPane.setAlignment(layout, Pos.CENTER);
    }

    private ImageView iconView(String imagePath, double size) {
        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    public Parent getRoot() {
        return root;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public String getSelectedRole() {
        return adminRoleButton.isSelected() ? "Admin" : "Staff";
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public void showStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-neutral", "status-error", "status-success");
        statusLabel.getStyleClass().add(error ? "status-error" : "status-success");
    }

    private VBox fieldGroup(String labelText, TextField field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        return new VBox(6, label, field);
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("login-title");
        return label;
    }

    private Label sectionCopy(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("login-copy");
        label.setWrapText(true);
        return label;
    }

    private HBox featureLine(String iconText, String text) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add("feature-bullet");
        Label label = new Label(text);
        label.getStyleClass().add("feature-text");
        label.setWrapText(true);
        HBox box = new HBox(12, icon, label);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private VBox statCard(String value, String labelText) {
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("hero-stat-value");
        Label titleLabel = new Label(labelText);
        titleLabel.getStyleClass().add("hero-stat-label");
        VBox card = new VBox(4, valueLabel, titleLabel);
        card.getStyleClass().add("hero-stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private VBox showcaseCard(String imagePath, String title) {
        ImageView imageView = iconView(imagePath, 34);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("showcase-title");
        VBox card = new VBox(10, imageView, titleLabel);
        card.getStyleClass().add("showcase-card");
        card.setAlignment(Pos.CENTER);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private Label pill(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("security-pill");
        return label;
    }

}
