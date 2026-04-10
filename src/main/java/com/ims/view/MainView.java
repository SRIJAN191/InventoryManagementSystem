package com.ims.view;

import com.ims.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

public class MainView {
    private final BorderPane root = new BorderPane();
    private final VBox sidebar = new VBox(10);
    private final Label welcomeLabel = new Label("Welcome, Guest");
    private final Label roleLabel = new Label("Viewer Session");
    private final Label pageTitleLabel = new Label("Dashboard");
    private final Label pageSubtitleLabel = new Label("Track inventory, sales, and performance in one place.");
    private final Label topChipLabel = new Label("Operational Snapshot");
    private final StackPane contentHost = new StackPane();
    private final ScrollPane contentScroll = new ScrollPane(contentHost);
    private final Button dashboardButton = createNavButton("/images/dashboard.png", "Dashboard");
    private final Button productsButton = createNavButton("/images/product.png", "Products");
    private final Button categoriesButton = createNavButton("/images/category.png", "Categories");
    private final Button staffButton = createNavButton("/images/report.png", "Staff");
    private final Button stockButton = createNavButton("/images/stock.png", "Stock");
    private final Button salesButton = createNavButton("/images/sales.png", "Sales");
    private final Button reportsButton = createNavButton("/images/report.png", "Reports");
    private final Button logoutButton = new Button("Sign Out");
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final DateTimeFormatter chipDateFormatter = DateTimeFormatter.ofPattern("dd MMM uuuu");

    public MainView() {
        root.getStyleClass().add("app-root");

        ImageView brandIcon = iconView("/images/dashboard.png", 26);
        Label brandMark = new Label();
        brandMark.setGraphic(brandIcon);
        brandMark.getStyleClass().add("brand-mark");
        Label brandTitle = new Label("Inventory Management Dashboard");
        brandTitle.getStyleClass().add("sidebar-title");
        brandTitle.setWrapText(true);
        brandTitle.setMaxWidth(Double.MAX_VALUE);
        Label brandSubtitle = new Label("Modern inventory workspace");
        brandSubtitle.getStyleClass().add("sidebar-subtitle");
        brandSubtitle.setWrapText(true);
        brandSubtitle.setMaxWidth(Double.MAX_VALUE);
        VBox brandBox = new VBox(4, brandTitle, brandSubtitle);
        brandBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(brandBox, Priority.ALWAYS);
        HBox brandRow = new HBox(14, brandMark, brandBox);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        brandRow.setMaxWidth(Double.MAX_VALUE);

        Label sessionBadge = new Label("LIVE SESSION");
        sessionBadge.getStyleClass().add("session-badge");
        welcomeLabel.getStyleClass().add("session-name");
        welcomeLabel.setWrapText(true);
        welcomeLabel.setMaxWidth(Double.MAX_VALUE);
        roleLabel.getStyleClass().add("session-role");
        roleLabel.setWrapText(true);
        roleLabel.setMaxWidth(Double.MAX_VALUE);
        VBox sessionCopy = new VBox(6, sessionBadge, welcomeLabel, roleLabel);
        sessionCopy.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(sessionCopy, Priority.ALWAYS);
        Label avatar = new Label(userInitials(null));
        avatar.getStyleClass().add("session-avatar");
        HBox sessionBox = new HBox(14, avatar, sessionCopy);
        sessionBox.setAlignment(Pos.CENTER_LEFT);
        sessionBox.getStyleClass().add("sidebar-session");
        sessionBox.setMaxWidth(Double.MAX_VALUE);

        navButtons.put("dashboard", dashboardButton);
        navButtons.put("products", productsButton);
        navButtons.put("categories", categoriesButton);
        navButtons.put("staff", staffButton);
        navButtons.put("stock", stockButton);
        navButtons.put("sales", salesButton);
        navButtons.put("reports", reportsButton);

        VBox navBox = new VBox(8,
            dashboardButton,
            productsButton,
            categoriesButton,
            staffButton,
            stockButton,
            salesButton,
            reportsButton
        );
        navBox.getStyleClass().add("sidebar-nav");

        Region sidebarSpacer = new Region();
        VBox.setVgrow(sidebarSpacer, Priority.ALWAYS);

        logoutButton.setGraphic(iconView("/images/report.png", 14));
        logoutButton.setContentDisplay(ContentDisplay.LEFT);
        logoutButton.setGraphicTextGap(10);
        logoutButton.getStyleClass().addAll("ghost-button", "sidebar-logout");
        sidebar.getStyleClass().add("sidebar");
        sidebar.getChildren().addAll(brandRow, sessionBox, navBox, sidebarSpacer, logoutButton);
        sidebar.setPadding(new Insets(22));
        sidebar.setMinWidth(300);
        sidebar.setPrefWidth(300);
        root.setLeft(sidebar);

        Label shellEyebrow = new Label("BUSINESS OVERVIEW");
        shellEyebrow.getStyleClass().add("app-eyebrow");
        pageTitleLabel.getStyleClass().add("app-title");
        pageSubtitleLabel.getStyleClass().add("app-subtitle");
        pageSubtitleLabel.setWrapText(true);
        VBox pageInfo = new VBox(6, shellEyebrow, pageTitleLabel, pageSubtitleLabel);

        topChipLabel.getStyleClass().add("top-chip");
        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);
        HBox header = new HBox(pageInfo, push, topChipLabel);
        header.getStyleClass().add("top-shell");
        header.setAlignment(Pos.CENTER_LEFT);

        contentHost.getStyleClass().add("content-host");
        contentHost.setAlignment(Pos.TOP_LEFT);
        contentHost.setMaxWidth(Double.MAX_VALUE);
        contentScroll.setFitToWidth(true);
        contentScroll.setFitToHeight(false);
        contentScroll.setPannable(true);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroll.getStyleClass().add("content-scroll");

        VBox centerShell = new VBox(16, header, contentScroll);
        centerShell.getStyleClass().add("app-shell");
        centerShell.setPadding(new Insets(18, 20, 20, 20));
        VBox.setVgrow(contentScroll, Priority.ALWAYS);
        root.setCenter(centerShell);
    }

    private Button createNavButton(String imagePath, String text) {
        Button button = new Button(text);
        button.setGraphic(iconView(imagePath, 18));
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(12);
        button.getStyleClass().add("sidebar-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }

    private ImageView iconView(String imagePath, double size) {
        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("nav-icon");
        return imageView;
    }

    private String userInitials(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            return "ID";
        }
        String username = user.getUsername().trim().toUpperCase();
        return username.length() >= 2 ? username.substring(0, 2) : username;
    }

    public Parent getRoot() {
        return root;
    }

    public void setCurrentUser(User user) {
        if (user == null) {
            welcomeLabel.setText("Welcome, Guest");
            roleLabel.setText("Viewer Session");
            topChipLabel.setText("Operational Snapshot | " + LocalDate.now().format(chipDateFormatter));
            return;
        }
        welcomeLabel.setText("Welcome, " + user.getUsername());
        roleLabel.setText(user.getRole() + " Session");
        topChipLabel.setText(user.getRole() + " Workspace | " + LocalDate.now().format(chipDateFormatter));
        ((Label) ((HBox) sidebar.getChildren().get(1)).getChildren().get(0)).setText(userInitials(user));
    }

    public void setOnLogout(Runnable action) {
        logoutButton.setOnAction(event -> action.run());
    }

    public void setSection(String key, String title, String subtitle, Parent content) {
        pageTitleLabel.setText(title);
        pageSubtitleLabel.setText(subtitle);
        if (content instanceof Region region) {
            region.setMinWidth(0);
            region.setMaxWidth(Double.MAX_VALUE);
        }
        contentHost.getChildren().setAll(content);
        setActiveSection(key);
    }

    public void setActiveSection(String key) {
        navButtons.values().forEach(button -> button.getStyleClass().remove("sidebar-button-active"));
        Button activeButton = navButtons.get(key);
        if (activeButton != null && !activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    public void setCategoryVisible(boolean visible) {
        categoriesButton.setManaged(visible);
        categoriesButton.setVisible(visible);
    }

    public void setStaffVisible(boolean visible) {
        staffButton.setManaged(visible);
        staffButton.setVisible(visible);
    }

    public void setReportsVisible(boolean visible) {
        reportsButton.setManaged(visible);
        reportsButton.setVisible(visible);
    }

    public Button getDashboardButton() {
        return dashboardButton;
    }

    public Button getProductsButton() {
        return productsButton;
    }

    public Button getCategoriesButton() {
        return categoriesButton;
    }

    public Button getStockButton() {
        return stockButton;
    }

    public Button getStaffButton() {
        return staffButton;
    }

    public Button getSalesButton() {
        return salesButton;
    }

    public Button getReportsButton() {
        return reportsButton;
    }
}
