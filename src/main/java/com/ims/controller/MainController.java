package com.ims.controller;

import com.ims.db.DatabaseConnection;
import com.ims.model.ActivityLogEntry;
import com.ims.model.Category;
import com.ims.model.DashboardMetrics;
import com.ims.model.InventoryReportRow;
import com.ims.model.Product;
import com.ims.model.ProductVariantEntry;
import com.ims.model.ProfitSummaryRow;
import com.ims.model.RestockRecommendation;
import com.ims.model.Sale;
import com.ims.model.User;
import com.ims.service.ActivityLogService;
import com.ims.service.DashboardService;
import com.ims.service.ExportService;
import com.ims.service.InventoryService;
import com.ims.service.ReportService;
import com.ims.service.UserService;
import com.ims.util.AlertUtil;
import com.ims.view.MainView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public class MainController {
    private final MainView view = new MainView();
    private final InventoryService inventoryService = new InventoryService();
    private final DashboardService dashboardService = new DashboardService();
    private final ReportService reportService = new ReportService();
    private final ExportService exportService = new ExportService();
    private final ActivityLogService activityLogService = new ActivityLogService();
    private final UserService userService = new UserService();
    private final User currentUser;
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN"));

    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private final ObservableList<ProductVariantEntry> productVariantRows = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<Sale> recentSales = FXCollections.observableArrayList();
    private final ObservableList<Sale> salesReportRows = FXCollections.observableArrayList();
    private final ObservableList<InventoryReportRow> inventoryRows = FXCollections.observableArrayList();
    private final ObservableList<ProfitSummaryRow> profitRows = FXCollections.observableArrayList();
    private final ObservableList<RestockRecommendation> restockRows = FXCollections.observableArrayList();
    private final ObservableList<ActivityLogEntry> activityRows = FXCollections.observableArrayList();
    private final ObservableList<User> staffUsers = FXCollections.observableArrayList();
    private final ObservableList<String> lowStockMessages = FXCollections.observableArrayList();
    private final ObservableList<String> dashboardRecentSales = FXCollections.observableArrayList();
    private final ObservableList<String> dashboardActivityMessages = FXCollections.observableArrayList();

    private final Label totalProductsValue = new Label("0");
    private final Label totalStockValue = new Label("Rs 0.00");
    private final Label todaysSalesValue = new Label("Rs 0.00");
    private final Label todaysProfitValue = new Label("Rs 0.00");
    private final Label lowStockValue = new Label("0");
    private final Label todaysSalesTitle = new Label("Today's Sales");
    private final Label todaysProfitTitle = new Label("Today's Profit");
    private final BarChart<String, Number> monthlyProfitChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
    private final PieChart categorySalesChart = new PieChart();
    private final ListView<String> lowStockList = new ListView<>(lowStockMessages);
    private final ListView<String> recentSalesList = new ListView<>(dashboardRecentSales);
    private final ListView<String> activityFeedList = new ListView<>(dashboardActivityMessages);
    private final TableView<Sale> dashboardTransactionsTable = new TableView<>(recentSales);

    private final TextField productSearchField = new TextField();
    private final ComboBox<String> productFilterCategory = new ComboBox<>();
    private final CheckBox productLowStockOnly = new CheckBox("Low stock only");
    private final Label productResultCountLabel = new Label("0 records");
    private final TableView<Product> productTable = new TableView<>(products);
    private final TableView<ProductVariantEntry> productVariantTable = new TableView<>(productVariantRows);
    private final TextField productNameField = new TextField();
    private final TextField variantField = new TextField();
    private final TextField colorField = new TextField();
    private final ComboBox<String> productCategoryBox = new ComboBox<>();
    private final TextField costPriceField = new TextField();
    private final TextField sellingPriceField = new TextField();
    private final TextField quantityField = new TextField();
    private final TextField minimumStockField = new TextField();
    private final Label productFormModeLabel = new Label("New Product");
    private final Label productFormHintLabel = new Label("Search a category, fill the stock details, add one row, then save.");
    private final Button addVariantButton = new Button("Add Variant");
    private final Button updateVariantButton = new Button("Update Variant");
    private final Button removeVariantButton = new Button("Remove Variant");
    private final Button clearVariantButton = new Button("Clear Variant");

    private final TextField categoryNameField = new TextField();
    private final TableView<Category> categoryTable = new TableView<>(categories);
    private final TextField staffSearchField = new TextField();
    private final Label staffResultCountLabel = new Label("0 records");
    private final TableView<User> staffTable = new TableView<>(staffUsers);
    private final TextField staffUsernameField = new TextField();
    private final ComboBox<String> staffRoleBox = new ComboBox<>();
    private final PasswordField staffPasswordField = new PasswordField();
    private final TextField staffPasswordVisibleField = new TextField();
    private final CheckBox staffShowPasswordCheck = new CheckBox("Show password");
    private final Label staffFormModeLabel = new Label("New Staff Account");
    private final Label staffFormHintLabel = new Label("Create staff login access for day-to-day inventory and sales work.");

    private final TextField saleProductSearchField = new TextField();
    private final ComboBox<Product> saleProductBox = new ComboBox<>();
    private final TextField saleQuantityField = new TextField();
    private final TextArea billPreview = new TextArea();
    private final TableView<Sale> salesHistoryTable = new TableView<>(recentSales);
    private final Button recordSaleButton = new Button("Record Sale");
    private final Button updateSaleButton = new Button("Update Sale");
    private final Button clearSaleButton = new Button("Clear");
    private final Button cancelSaleEditButton = new Button("Cancel Edit");
    private final Button editSaleButton = new Button("Edit Selected Sale");
    private final Button removeSaleButton = new Button("Remove Selected Sale");

    private final TextField inventorySearchField = new TextField();
    private final ComboBox<String> inventoryCategoryFilter = new ComboBox<>();
    private final ComboBox<String> inventorySortFilter = new ComboBox<>();
    private final CheckBox inventoryOutOfStockOnly = new CheckBox("Out of stock only");
    private final Label inventoryResultCountLabel = new Label("0 records");
    private final TableView<InventoryReportRow> inventoryReportTable = new TableView<>(inventoryRows);

    private final DatePicker salesFromDate = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker salesToDate = new DatePicker(LocalDate.now());
    private final TextField salesSearchField = new TextField();
    private final Label salesResultCountLabel = new Label("0 records");
    private final Label salesTotalRevenueValue = new Label("Rs 0.00");
    private final Label salesTotalProfitValue = new Label("Rs 0.00");
    private final TableView<Sale> salesReportTable = new TableView<>(salesReportRows);

    private final ComboBox<String> profitModeBox = new ComboBox<>();
    private final DatePicker profitFromDate = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker profitToDate = new DatePicker(LocalDate.now());
    private final Label profitResultCountLabel = new Label("0 records");
    private final Label profitTotalSalesValue = new Label("Rs 0.00");
    private final Label profitTotalProfitValue = new Label("Rs 0.00");
    private final TableView<ProfitSummaryRow> profitReportTable = new TableView<>(profitRows);
    private final TableView<RestockRecommendation> restockPlannerTable = new TableView<>(restockRows);
    private final TableView<ActivityLogEntry> activityLogTable = new TableView<>(activityRows);
    private final Label restockSnapshotLabel = new Label("No reorder actions needed.");

    private boolean syncingProductCategoryFilter;
    private boolean syncingInventoryCategoryFilter;

    private Parent dashboardPage;
    private Parent productsPage;
    private Parent categoriesPage;
    private Parent staffPage;
    private Parent stockPage;
    private Parent salesPage;
    private Parent reportsPage;
    private Sale editingSale;

    public MainController(User currentUser, Runnable logoutAction) {
        this.currentUser = currentUser == null ? new User(0, "admin", "", "Admin") : currentUser;
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(2);

        view.setCurrentUser(this.currentUser);
        view.setOnLogout(logoutAction == null ? () -> {
        } : logoutAction);
        view.setCategoryVisible(hasManagementAccess());
        view.setStaffVisible(hasManagementAccess());
        view.setReportsVisible(hasReportAccess());

        configureNavigation();
        buildPages();
        logActivity("Session Started", "Dashboard Access", "Opened workspace with " + this.currentUser.getRole() + " permissions.");
        refreshAll();
        showDashboard();
    }

    public Parent getView() {
        return view.getRoot();
    }

    private void configureNavigation() {
        view.getDashboardButton().setOnAction(event -> showDashboard());
        view.getProductsButton().setOnAction(event -> showProducts());
        view.getCategoriesButton().setOnAction(event -> showCategories());
        view.getStaffButton().setOnAction(event -> showStaff());
        view.getStockButton().setOnAction(event -> showStock());
        view.getSalesButton().setOnAction(event -> showSales());
        view.getReportsButton().setOnAction(event -> showReports());
    }

    private void buildPages() {
        dashboardPage = buildDashboardPage();
        productsPage = buildProductsPage();
        categoriesPage = buildCategoriesPage();
        staffPage = buildStaffPage();
        stockPage = buildStockPage();
        salesPage = buildSalesPage();
        reportsPage = buildReportsPage();
    }

    private Parent buildDashboardPage() {
        boolean showProfitWidgets = hasReportAccess();
        monthlyProfitChart.setLegendVisible(false);
        monthlyProfitChart.setAnimated(false);
        monthlyProfitChart.setTitle("Monthly Profit");
        monthlyProfitChart.setMinHeight(320);

        categorySalesChart.setTitle("Category Sales");
        categorySalesChart.setMinHeight(320);

        configureDashboardTransactionsTable();
        lowStockList.setPrefHeight(210);
        recentSalesList.setPrefHeight(210);
        activityFeedList.setPrefHeight(210);

        FlowPane metricRow = new FlowPane();
        metricRow.setHgap(18);
        metricRow.setVgap(18);
        metricRow.getChildren().addAll(
            metricCard("/images/product.png", "Total Products", totalProductsValue, "card-blue"),
            metricCard("/images/stock.png", "Stock Value", totalStockValue, "card-gold"),
            metricCard("/images/sales.png", todaysSalesTitle, todaysSalesValue, "card-coral")
        );
        if (showProfitWidgets) {
            metricRow.getChildren().add(metricCard("/images/report.png", todaysProfitTitle, todaysProfitValue, "card-green"));
        }
        metricRow.getChildren().add(metricCard("/images/category.png", "Low Stock", lowStockValue, "card-red"));
        metricRow.getStyleClass().add("summary-strip");

        FlowPane signalRow = new FlowPane();
        signalRow.setHgap(18);
        signalRow.setVgap(18);
        VBox lowStockPanel = wrapPanel("Low Stock Alert", lowStockList);
        VBox recentSalesPanel = wrapPanel("Recent Sales", recentSalesList);
        VBox activityPanel = wrapPanel("Activity Feed", activityFeedList);
        lowStockPanel.getStyleClass().add("dashboard-half-panel");
        recentSalesPanel.getStyleClass().add("dashboard-half-panel");
        activityPanel.getStyleClass().add("dashboard-third-panel");
        signalRow.getChildren().addAll(
            lowStockPanel,
            recentSalesPanel,
            activityPanel
        );

        VBox root = new VBox(18, metricRow, signalRow);
        if (showProfitWidgets) {
            FlowPane chartRow = new FlowPane();
            chartRow.setHgap(18);
            chartRow.setVgap(18);
            VBox salesOverviewPanel = wrapPanel("Sales Overview", monthlyProfitChart);
            VBox profitAnalysisPanel = wrapPanel("Profit Analysis", categorySalesChart);
            salesOverviewPanel.getStyleClass().add("dashboard-half-panel");
            profitAnalysisPanel.getStyleClass().add("dashboard-half-panel");
            chartRow.getChildren().addAll(
                salesOverviewPanel,
                profitAnalysisPanel
            );
            root.getChildren().add(chartRow);
        }
        root.getChildren().add(wrapPanel("Recent Transactions", dashboardTransactionsTable));
        root.getStyleClass().add("page-stack");
        return root;
    }

    private Parent buildProductsPage() {
        productSearchField.setPromptText("Search product, variant, or color");
        variantField.setPromptText("e.g. XL, 128GB, Premium");
        colorField.setPromptText("e.g. Black, Blue");
        productFilterCategory.setPromptText("Filter category");
        productCategoryBox.setPromptText("Choose category");
        productCategoryBox.setEditable(false);
        productCategoryBox.setVisibleRowCount(8);

        productSearchField.textProperty().addListener((obs, oldValue, newValue) -> refreshProducts());
        productFilterCategory.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!syncingProductCategoryFilter) {
                refreshProducts();
            }
        });
        productLowStockOnly.selectedProperty().addListener((obs, oldValue, selected) -> refreshProducts());
        productLowStockOnly.getStyleClass().add("toolbar-check");
        productResultCountLabel.getStyleClass().add("toolbar-meta");
        productFormModeLabel.getStyleClass().add("form-mode-title");
        productFormModeLabel.setWrapText(true);
        productFormHintLabel.getStyleClass().add("form-helper-text");
        productFormHintLabel.setWrapText(true);

        productTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        addColumn(productTable, "ID", Product::getProductId);
        addColumn(productTable, "Product", Product::getProductName);
        addColumn(productTable, "Variant", Product::getVariant);
        addColumn(productTable, "Color", Product::getColor);
        addColumn(productTable, "Category", Product::getCategory);
        addColumn(productTable, "Cost Price", product -> money(product.getCostPrice()));
        addColumn(productTable, "Selling Price", product -> money(product.getSellingPrice()));
        addColumn(productTable, "Quantity", Product::getQuantity);
        addColumn(productTable, "Min Stock", Product::getMinimumStock);
        productTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("low-stock-row");
                if (!empty && item != null && item.isBelowMinimumStock()) {
                    getStyleClass().add("low-stock-row");
                }
            }
        });
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> populateProductForm(selected));

        productVariantTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        productVariantTable.setPrefHeight(165);
        addColumn(productVariantTable, "Variant", row -> blankLabel(row.getVariant()));
        addColumn(productVariantTable, "Color", row -> blankLabel(row.getColor()));
        addColumn(productVariantTable, "Cost", row -> money(row.getCostPrice()));
        addColumn(productVariantTable, "Selling", row -> money(row.getSellingPrice()));
        addColumn(productVariantTable, "Qty", ProductVariantEntry::getQuantity);
        addColumn(productVariantTable, "Min", ProductVariantEntry::getMinimumStock);
        productVariantTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            populateVariantEditor(selected);
            updateVariantActionState();
        });

        Button saveButton = new Button("Save");
        Button deleteButton = new Button("Delete");
        Button clearButton = new Button("New");
        addVariantButton.setText("Add Row");
        updateVariantButton.setText("Update Row");
        removeVariantButton.setText("Remove Row");
        clearVariantButton.setText("Clear Row");
        saveButton.getStyleClass().add("primary-product-button");
        clearButton.getStyleClass().add("secondary-product-button");
        saveButton.setOnAction(event -> saveProduct());
        deleteButton.setOnAction(event -> deleteProduct());
        clearButton.setOnAction(event -> clearProductForm());
        addVariantButton.setOnAction(event -> addVariantRow());
        updateVariantButton.setOnAction(event -> updateVariantRow());
        removeVariantButton.setOnAction(event -> removeVariantRow());
        clearVariantButton.setOnAction(event -> clearVariantEditor());
        updateVariantActionState();

        GridPane form = new GridPane();
        form.getStyleClass().add("form-grid");
        form.setHgap(14);
        form.setVgap(14);
        form.addRow(0, labeledField("Product Name", productNameField), labeledField("Category", productCategoryBox));
        form.addRow(1, labeledField("Variant", variantField), labeledField("Color", colorField));
        form.addRow(2, labeledField("Cost Price", costPriceField), labeledField("Selling Price", sellingPriceField));
        form.addRow(3, labeledField("Quantity", quantityField), labeledField("Minimum Stock", minimumStockField));
        FlowPane actions = actionWrap(saveButton, clearButton, deleteButton);
        form.add(new VBox(6, productFormModeLabel, productFormHintLabel, actions), 0, 4, 2, 1);
        form.add(
            labeledField("Variant Rows", new VBox(10,
                actionWrap(addVariantButton, updateVariantButton, removeVariantButton, clearVariantButton),
                productVariantTable
            )),
            0, 5, 2, 1
        );

        VBox left = new VBox(14,
            toolbar(productSearchField, productFilterCategory, productLowStockOnly, productResultCountLabel),
            wrapPanel("Product Inventory", productTable)
        );
        VBox right = new VBox(14, wrapPanel("Product Form", form));
        right.setMinWidth(430);
        right.setPrefWidth(470);
        left.setPrefWidth(820);
        SplitPane splitPane = new SplitPane(left, right);
        splitPane.setDividerPositions(0.71);
        splitPane.getStyleClass().add("content-split");
        return splitPane;
    }

    private Parent buildCategoriesPage() {
        if (!hasManagementAccess()) {
            return accessPanel("Category management is available only for Admin accounts.");
        }

        categoryNameField.setPromptText("Enter category name");
        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        addColumn(categoryTable, "Category Name", Category::categoryName);
        categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            categoryNameField.setText(selected == null ? "" : selected.categoryName());
            categoryNameField.getStyleClass().remove("invalid-field");
        });

        Button addCategoryButton = new Button("Add Category");
        Button deleteCategoryButton = new Button("Delete Category");
        addCategoryButton.setOnAction(event -> addCategory());
        deleteCategoryButton.setOnAction(event -> deleteCategory());

        VBox left = new VBox(14, wrapPanel("Category List", categoryTable));
        VBox right = new VBox(14,
            wrapPanel("Manage Categories", new VBox(14,
                labeledField("Category Name", categoryNameField),
                actionWrap(addCategoryButton, deleteCategoryButton)
            ))
        );
        left.setPrefWidth(760);
        SplitPane splitPane = new SplitPane(left, right);
        splitPane.setDividerPositions(0.67);
        splitPane.getStyleClass().add("content-split");
        return splitPane;
    }

    private Parent buildStaffPage() {
        if (!hasManagementAccess()) {
            return accessPanel("Staff management is available only for Admin accounts.");
        }

        staffSearchField.setPromptText("Search username");
        staffSearchField.textProperty().addListener((obs, oldValue, newValue) -> refreshStaffData());
        staffResultCountLabel.getStyleClass().add("toolbar-meta");
        staffUsernameField.setPromptText("Enter staff username");
        staffRoleBox.getItems().setAll("Staff", "Admin");
        staffRoleBox.setValue("Staff");
        staffPasswordField.setPromptText("Enter password");
        staffPasswordVisibleField.setPromptText("Enter password");
        staffPasswordVisibleField.textProperty().bindBidirectional(staffPasswordField.textProperty());
        staffPasswordField.managedProperty().bind(staffShowPasswordCheck.selectedProperty().not());
        staffPasswordField.visibleProperty().bind(staffShowPasswordCheck.selectedProperty().not());
        staffPasswordVisibleField.managedProperty().bind(staffShowPasswordCheck.selectedProperty());
        staffPasswordVisibleField.visibleProperty().bind(staffShowPasswordCheck.selectedProperty());
        staffShowPasswordCheck.getStyleClass().add("toolbar-check");
        staffFormModeLabel.getStyleClass().add("form-mode-title");
        staffFormHintLabel.getStyleClass().add("form-helper-text");
        staffFormHintLabel.setWrapText(true);

        staffTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        staffTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        addColumn(staffTable, "ID", User::getUserId);
        addColumn(staffTable, "Username", User::getUsername);
        addColumn(staffTable, "Role", User::getRole);
        staffTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> populateStaffForm(selected));

        Button saveStaffButton = new Button("Save Staff");
        Button clearStaffButton = new Button("New Staff");
        Button deleteStaffButton = new Button("Delete Staff");
        saveStaffButton.setOnAction(event -> saveStaff());
        clearStaffButton.setOnAction(event -> clearStaffForm());
        deleteStaffButton.setOnAction(event -> deleteStaff());

        VBox left = new VBox(14,
            toolbar(staffSearchField, staffResultCountLabel),
            wrapPanel("Staff Accounts", staffTable)
        );
        VBox right = new VBox(14,
            wrapPanel("Manage Staff", new VBox(14,
                staffFormModeLabel,
                staffFormHintLabel,
                labeledField("Username", staffUsernameField),
                labeledField("Role", staffRoleBox),
                labeledField("Password", new VBox(8, new StackPane(staffPasswordField, staffPasswordVisibleField), staffShowPasswordCheck)),
                actionWrap(saveStaffButton, clearStaffButton, deleteStaffButton)
            ))
        );
        left.setPrefWidth(760);
        right.setMinWidth(360);
        right.setPrefWidth(390);
        SplitPane splitPane = new SplitPane(left, right);
        splitPane.setDividerPositions(0.68);
        splitPane.getStyleClass().add("content-split");
        return splitPane;
    }

    private Parent buildStockPage() {
        inventorySearchField.setPromptText("Search stock");
        inventorySearchField.textProperty().addListener((obs, oldValue, newValue) -> refreshInventoryReport());
        inventoryCategoryFilter.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!syncingInventoryCategoryFilter) {
                refreshInventoryReport();
            }
        });
        inventorySortFilter.getItems().setAll("Name", "Stock", "Value");
        inventorySortFilter.setValue("Name");
        inventorySortFilter.valueProperty().addListener((obs, oldValue, newValue) -> refreshInventoryReport());
        inventoryOutOfStockOnly.selectedProperty().addListener((obs, oldValue, selected) -> refreshInventoryReport());
        inventoryOutOfStockOnly.getStyleClass().add("toolbar-check");
        inventoryResultCountLabel.getStyleClass().add("toolbar-meta");

        inventoryReportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        addColumn(inventoryReportTable, "Product", InventoryReportRow::product);
        addColumn(inventoryReportTable, "Stock", InventoryReportRow::stock);
        addColumn(inventoryReportTable, "Cost Price", row -> money(row.costPrice()));
        addColumn(inventoryReportTable, "Total Value", row -> money(row.totalValue()));

        VBox root = new VBox(14,
            toolbar(inventorySearchField, inventoryCategoryFilter, inventorySortFilter, inventoryOutOfStockOnly, inventoryResultCountLabel),
            wrapPanel("Stock Inventory", inventoryReportTable)
        );
        root.getStyleClass().add("page-stack");
        return root;
    }

    private Parent buildSalesPage() {
        saleProductSearchField.setPromptText("Search product");
        saleProductSearchField.textProperty().addListener((obs, oldValue, newValue) -> refreshSalesChoices());
        saleProductBox.setPromptText("Select product");
        saleProductBox.setVisibleRowCount(8);
        saleProductBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Product product) {
                return productLabel(product);
            }

            @Override
            public Product fromString(String string) {
                return null;
            }
        });
        saleProductBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : productLabel(item));
            }
        });
        saleProductBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : productLabel(item));
            }
        });
        saleQuantityField.setPromptText("Quantity");
        billPreview.setEditable(false);
        billPreview.setPrefRowCount(7);
        billPreview.setPrefHeight(190);
        billPreview.setWrapText(true);

        recordSaleButton.setOnAction(event -> recordSale());
        updateSaleButton.setOnAction(event -> updateSale());
        clearSaleButton.setOnAction(event -> clearSaleForm());
        cancelSaleEditButton.setOnAction(event -> clearSaleForm());
        editSaleButton.setOnAction(event -> startEditingSelectedSale());
        removeSaleButton.setOnAction(event -> deleteSelectedSale());

        FlowPane saleActions = actionWrap(recordSaleButton, clearSaleButton, updateSaleButton, cancelSaleEditButton);

        VBox salesForm = new VBox(14,
            labeledField("Search", saleProductSearchField),
            labeledField("Product", saleProductBox),
            labeledField("Quantity", saleQuantityField),
            saleActions
        );
        salesForm.setFillWidth(true);

        salesHistoryTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        salesHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        addColumn(salesHistoryTable, "Date", sale -> sale.getSaleDate().toString());
        addColumn(salesHistoryTable, "Product", Sale::getProductName);
        addColumn(salesHistoryTable, "Qty", Sale::getQuantity);
        addColumn(salesHistoryTable, "Revenue", sale -> money(sale.getRevenue()));
        addColumn(salesHistoryTable, "Profit", sale -> money(sale.getProfit()));
        salesHistoryTable.setPrefHeight(320);
        salesHistoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> updateSaleSelectionState());

        VBox salesEntryPanel = wrapPanel("Sales Entry", salesForm);
        salesEntryPanel.getStyleClass().add("sales-top-panel");
        salesEntryPanel.setMinWidth(360);
        salesEntryPanel.setPrefWidth(390);
        VBox billPanel = wrapPanel("Bill Preview", billPreview);
        billPanel.getStyleClass().add("sales-top-panel");
        billPanel.getStyleClass().add("sales-bill-panel");
        billPanel.setMinWidth(360);
        HBox.setHgrow(billPanel, Priority.ALWAYS);

        FlowPane top = new FlowPane(18, 18,
            salesEntryPanel,
            billPanel
        );
        top.getStyleClass().add("sales-top-row");
        top.setPrefWrapLength(1040);
        HBox.setHgrow(billPanel, Priority.ALWAYS);

        FlowPane recentSaleActions = actionWrap(editSaleButton, removeSaleButton);

        VBox recentSalesPanel = wrapPanel("Recent Sales List", new VBox(12, recentSaleActions, salesHistoryTable));
        recentSalesPanel.getStyleClass().add("table-panel");

        VBox root = new VBox(18,
            top,
            recentSalesPanel
        );
        VBox.setVgrow(root.getChildren().get(1), Priority.ALWAYS);
        root.getStyleClass().add("page-stack");
        setSaleEditMode(false);
        updateSaleSelectionState();
        return root;
    }

    private Parent buildReportsPage() {
        salesSearchField.setPromptText("Search product name");
        salesSearchField.setMinWidth(190);
        salesSearchField.setPrefWidth(190);
        salesFromDate.setMinWidth(150);
        salesFromDate.setPrefWidth(150);
        salesToDate.setMinWidth(150);
        salesToDate.setPrefWidth(150);
        salesSearchField.setOnAction(event -> refreshSalesReport());
        salesSearchField.textProperty().addListener((obs, oldValue, newValue) -> refreshSalesReport());
        salesFromDate.valueProperty().addListener((obs, oldValue, newValue) -> refreshSalesReport());
        salesToDate.valueProperty().addListener((obs, oldValue, newValue) -> refreshSalesReport());
        salesResultCountLabel.getStyleClass().add("toolbar-meta");

        salesReportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        addColumn(salesReportTable, "Date", sale -> sale.getSaleDate().toString());
        addColumn(salesReportTable, "Product", Sale::getProductName);
        addColumn(salesReportTable, "Qty Sold", Sale::getQuantity);
        addColumn(salesReportTable, "Revenue", sale -> money(sale.getRevenue()));
        addColumn(salesReportTable, "Profit", sale -> money(sale.getProfit()));

        profitModeBox.getItems().setAll("Daily", "Weekly", "Monthly");
        profitModeBox.setMinWidth(130);
        profitModeBox.setPrefWidth(130);
        profitFromDate.setMinWidth(150);
        profitFromDate.setPrefWidth(150);
        profitToDate.setMinWidth(150);
        profitToDate.setPrefWidth(150);
        profitModeBox.setValue("Daily");
        profitModeBox.valueProperty().addListener((obs, oldValue, newValue) -> refreshProfitReport());
        profitFromDate.valueProperty().addListener((obs, oldValue, newValue) -> refreshProfitReport());
        profitToDate.valueProperty().addListener((obs, oldValue, newValue) -> refreshProfitReport());
        profitResultCountLabel.getStyleClass().add("toolbar-meta");
        profitReportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        addColumn(profitReportTable, "Period", ProfitSummaryRow::period);
        addColumn(profitReportTable, "Profit", row -> money(row.profit()));

        restockPlannerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        addColumn(restockPlannerTable, "Product", RestockRecommendation::productLabel);
        addColumn(restockPlannerTable, "Category", RestockRecommendation::category);
        addColumn(restockPlannerTable, "Stock", RestockRecommendation::currentStock);
        addColumn(restockPlannerTable, "Min", RestockRecommendation::minimumStock);
        addColumn(restockPlannerTable, "Suggested Order", RestockRecommendation::suggestedOrder);
        addColumn(restockPlannerTable, "Urgency", RestockRecommendation::urgency);
        addColumn(restockPlannerTable, "Estimated Cost", row -> money(row.estimatedCost()));
        addColumn(restockPlannerTable, "Potential Profit", row -> money(row.projectedProfit()));
        restockPlannerTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(RestockRecommendation item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-critical", "status-high", "status-monitor");
                if (empty || item == null) {
                    return;
                }
                switch (item.urgency()) {
                    case "Critical" -> getStyleClass().add("status-critical");
                    case "High" -> getStyleClass().add("status-high");
                    case "Monitor" -> getStyleClass().add("status-monitor");
                    default -> {
                    }
                }
            }
        });

        activityLogTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        addColumn(activityLogTable, "Time", ActivityLogEntry::formattedTimestamp);
        addColumn(activityLogTable, "User", ActivityLogEntry::performedBy);
        addColumn(activityLogTable, "Action", ActivityLogEntry::actionType);
        addColumn(activityLogTable, "Subject", ActivityLogEntry::subject);
        addColumn(activityLogTable, "Details", ActivityLogEntry::details);

        Button searchSalesButton = new Button("Search");
        searchSalesButton.setOnAction(event -> refreshSalesReport());
        Button resetSalesButton = new Button("Reset");
        resetSalesButton.getStyleClass().add("secondary-product-button");
        resetSalesButton.setOnAction(event -> resetSalesReportFilters());
        Button exportSalesButton = new Button("Export CSV");
        exportSalesButton.setOnAction(event -> exportSalesReport());
        Button todaySalesButton = new Button("Today");
        todaySalesButton.getStyleClass().add("secondary-product-button");
        todaySalesButton.setOnAction(event -> applySalesDatePreset("TODAY"));
        Button weekSalesButton = new Button("This Week");
        weekSalesButton.getStyleClass().add("secondary-product-button");
        weekSalesButton.setOnAction(event -> applySalesDatePreset("WEEK"));
        Button monthSalesButton = new Button("This Month");
        monthSalesButton.getStyleClass().add("secondary-product-button");
        monthSalesButton.setOnAction(event -> applySalesDatePreset("MONTH"));
        Button todayProfitButton = new Button("Today");
        todayProfitButton.getStyleClass().add("secondary-product-button");
        todayProfitButton.setOnAction(event -> applyProfitDatePreset("TODAY"));
        Button weekProfitButton = new Button("This Week");
        weekProfitButton.getStyleClass().add("secondary-product-button");
        weekProfitButton.setOnAction(event -> applyProfitDatePreset("WEEK"));
        Button monthProfitButton = new Button("This Month");
        monthProfitButton.getStyleClass().add("secondary-product-button");
        monthProfitButton.setOnAction(event -> applyProfitDatePreset("MONTH"));
        Button resetProfitButton = new Button("Reset");
        resetProfitButton.getStyleClass().add("secondary-product-button");
        resetProfitButton.setOnAction(event -> resetProfitReportFilters());
        Button exportProfitButton = new Button("Export CSV");
        exportProfitButton.setOnAction(event -> exportProfitReport());
        Button exportRestockButton = new Button("Export CSV");
        exportRestockButton.setOnAction(event -> exportRestockReport());
        Button exportActivityButton = new Button("Export CSV");
        exportActivityButton.setOnAction(event -> exportActivityReport());
        Button backupDatabaseButton = new Button("Backup Database");
        backupDatabaseButton.setOnAction(event -> backupDatabase());

        Tab salesTab = reportTab("Sales Report", new VBox(14,
            toolbar(salesFromDate, salesToDate, todaySalesButton, weekSalesButton, monthSalesButton, salesSearchField, searchSalesButton, resetSalesButton, exportSalesButton, salesResultCountLabel),
            reportSummaryStrip(
                reportSummaryCard("Total Revenue", salesTotalRevenueValue),
                reportSummaryCard("Total Profit", salesTotalProfitValue)
            ),
            wrapPanel("Sales Report", salesReportTable)
        ));
        Tab profitTab = reportTab("Profit Report", new VBox(14,
            toolbar(profitModeBox, profitFromDate, profitToDate, todayProfitButton, weekProfitButton, monthProfitButton, resetProfitButton, exportProfitButton, profitResultCountLabel),
            wrapPanel("Profit Summary", profitReportTable),
            reportSummaryStrip(
                reportSummaryCard("Total Sales", profitTotalSalesValue),
                reportSummaryCard("Total Profit", profitTotalProfitValue)
            )
        ));
        Tab restockTab = reportTab("Restock Planner", new VBox(14,
            wrapPanel("Restock Snapshot", restockSnapshotLabel),
            toolbar(exportRestockButton),
            wrapPanel("Smart Restock Planner", restockPlannerTable)
        ));
        Tab activityTab = reportTab("Activity Center", new VBox(14,
            toolbar(exportActivityButton, backupDatabaseButton),
            wrapPanel("Operational Activity Log", activityLogTable)
        ));

        TabPane reportTabs = new TabPane(salesTab, profitTab, restockTab, activityTab);
        reportTabs.getStyleClass().add("inner-tabs");
        return reportTabs;
    }

    private void showDashboard() {
        String subtitle = hasReportAccess()
            ? "Watch sales, profit, stock movement, and quick alerts from a single view."
            : "Watch sales, stock movement, and quick alerts from a single view.";
        view.setSection("dashboard", "Dashboard", subtitle, dashboardPage);
    }

    private void showProducts() {
        view.setSection("products", "Products", "Manage inventory items, assign categories, and update pricing in one form.", productsPage);
    }

    private void showCategories() {
        if (!hasManagementAccess()) {
            AlertUtil.warning("Access Restricted", "Only admin can manage categories.");
            return;
        }
        view.setSection("categories", "Categories", "Create, remove, and maintain the categories available to products.", categoriesPage);
    }

    private void showStaff() {
        if (!hasManagementAccess()) {
            AlertUtil.warning("Access Restricted", "Only admin can manage staff accounts.");
            return;
        }
        view.setSection("staff", "Staff", "Create, update, and remove staff login accounts for operational users.", staffPage);
    }

    private void showStock() {
        view.setSection("stock", "Stock", "Browse current inventory levels with filtering, sorting, and scrolling.", stockPage);
    }

    private void showSales() {
        view.setSection("sales", "Sales", "Record transactions quickly and review the latest sales activity.", salesPage);
    }

    private void showReports() {
        if (!hasReportAccess()) {
            AlertUtil.warning("Access Restricted", "Only admin can view reports.");
            return;
        }
        view.setSection("reports", "Reports", "Review sales and profit reports and export clean business summaries.", reportsPage);
    }

    private void refreshAll() {
        refreshCategoryData();
        refreshStaffData();
        refreshProducts();
        refreshSalesChoices();
        refreshRecentSales();
        refreshActivityFeed();
        refreshDashboard();
        refreshInventoryReport();
        refreshSalesReport();
        refreshProfitReport();
        refreshRestockReport();
    }

    private void refreshCategoryData() {
        categories.setAll(inventoryService.getAllCategories());

        List<String> categoryFilters = inventoryService.getCategories();
        syncingProductCategoryFilter = true;
        try {
            retainSelection(productFilterCategory, categoryFilters, "All");
        } finally {
            syncingProductCategoryFilter = false;
        }

        syncingInventoryCategoryFilter = true;
        try {
            retainSelection(inventoryCategoryFilter, categoryFilters, "All");
        } finally {
            syncingInventoryCategoryFilter = false;
        }

        List<String> formCategories = inventoryService.getCategoryNames();
        String currentCategory = selectedProductCategory();
        productCategoryBox.getItems().setAll(formCategories);
        if (formCategories.contains(currentCategory)) {
            productCategoryBox.setValue(currentCategory);
        } else if (currentCategory.isBlank()) {
            productCategoryBox.setValue(null);
        } else {
            productCategoryBox.setValue(null);
        }
    }

    private void refreshStaffData() {
        staffUsers.setAll(userService.getStaffMembers(staffSearchField.getText()));
        staffResultCountLabel.setText(staffUsers.size() + (staffUsers.size() == 1 ? " record" : " records"));
    }

    private void refreshDashboard() {
        DashboardMetrics metrics = dashboardService.getMetrics();
        totalProductsValue.setText(String.valueOf(metrics.totalProducts()));
        totalStockValue.setText(money(metrics.totalStockValue()));
        todaysSalesValue.setText(money(metrics.todaysSales()));
        todaysProfitValue.setText(money(metrics.todaysProfit()));
        lowStockValue.setText(String.valueOf(metrics.lowStockItems()));
        String todayLabel = "Today's Sales (" + LocalDate.now() + ")";
        String profitLabel = "Today's Profit (" + LocalDate.now() + ")";
        todaysSalesTitle.setText(todayLabel);
        todaysProfitTitle.setText(profitLabel);

        lowStockMessages.setAll(dashboardService.lowStockProducts().stream()
            .map(product -> product.getDisplayName() + " - " + product.getQuantity() + " left (Min " + product.getMinimumStock() + ")")
            .toList());
        if (lowStockMessages.isEmpty()) {
            lowStockMessages.setAll("All products are above minimum stock.");
        }

        dashboardRecentSales.setAll(recentSales.stream()
            .limit(5)
            .map(sale -> sale.getProductName() + " - " + sale.getQuantity() + " pcs - " + money(sale.getRevenue()))
            .toList());
        if (dashboardRecentSales.isEmpty()) {
            dashboardRecentSales.setAll("No sales recorded yet.");
        }

        dashboardActivityMessages.setAll(activityRows.stream()
            .limit(6)
            .map(ActivityLogEntry::summary)
            .toList());
        if (dashboardActivityMessages.isEmpty()) {
            dashboardActivityMessages.setAll("No operational activity recorded yet.");
        }

        XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
        dashboardService.monthlyProfit().forEach(row -> profitSeries.getData().add(new XYChart.Data<>(row.period(), row.profit())));
        monthlyProfitChart.getData().clear();
        monthlyProfitChart.getData().add(profitSeries);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        dashboardService.categorySales().forEach(row -> pieData.add(new PieChart.Data(row.period(), row.profit())));
        categorySalesChart.setData(pieData);
    }

    private void refreshProducts() {
        List<Product> filteredProducts = inventoryService.getProducts(productSearchField.getText(), selectedOrAll(productFilterCategory));
        if (productLowStockOnly.isSelected()) {
            filteredProducts = filteredProducts.stream()
                .filter(Product::isBelowMinimumStock)
                .toList();
        }
        products.setAll(filteredProducts);
        productResultCountLabel.setText(products.size() + " records");
    }

    private void refreshSalesChoices() {
        String search = normalizedText(saleProductSearchField.getText()).toLowerCase(Locale.ROOT);
        Product currentSelection = saleProductBox.getValue();
        List<Product> availableProducts = inventoryService.getProducts("", "All").stream()
            .filter(product -> search.isBlank()
                || product.getDisplayName().toLowerCase(Locale.ROOT).contains(search)
                || normalizedText(product.getProductName()).toLowerCase(Locale.ROOT).contains(search)
                || normalizedText(product.getVariant()).toLowerCase(Locale.ROOT).contains(search)
                || normalizedText(product.getColor()).toLowerCase(Locale.ROOT).contains(search)
                || product.getCategory().toLowerCase(Locale.ROOT).contains(search))
            .toList();
        saleProductBox.getItems().setAll(availableProducts);
        Product refreshedSelection = currentSelection == null ? null : availableProducts.stream()
            .filter(product -> product.getProductId() == currentSelection.getProductId())
            .findFirst()
            .orElse(null);
        if (refreshedSelection != null) {
            saleProductBox.setValue(refreshedSelection);
        } else if (availableProducts.size() == 1) {
            saleProductBox.setValue(availableProducts.getFirst());
        } else if (!search.isBlank()) {
            saleProductBox.setValue(null);
        }

        if (search.isBlank()) {
            saleProductBox.hide();
        } else if (availableProducts.isEmpty()) {
            saleProductBox.hide();
        } else {
            saleProductBox.show();
        }
    }

    private void refreshRecentSales() {
        recentSales.setAll(dashboardService.recentSales(20));
    }

    private void refreshInventoryReport() {
        List<InventoryReportRow> rows = reportService.inventoryReport(
            inventorySearchField.getText(),
            selectedOrAll(inventoryCategoryFilter),
            inventorySortFilter.getValue() == null ? "Name" : inventorySortFilter.getValue()
        );
        if (inventoryOutOfStockOnly.isSelected()) {
            rows = rows.stream().filter(row -> row.stock() <= 0).toList();
        }
        inventoryRows.setAll(rows);
        inventoryResultCountLabel.setText(inventoryRows.size() + " records");
    }

    private void refreshSalesReport() {
        LocalDate from = salesFromDate.getValue() == null ? LocalDate.now().withDayOfMonth(1) : salesFromDate.getValue();
        LocalDate to = salesToDate.getValue() == null ? LocalDate.now() : salesToDate.getValue();
        if (from.isAfter(to)) {
            to = from;
            salesToDate.setValue(to);
        }
        salesReportRows.setAll(reportService.salesReport(from, to, salesSearchField.getText()));
        salesResultCountLabel.setText(salesReportRows.size() + " records");
        salesTotalRevenueValue.setText(money(salesReportRows.stream().mapToDouble(Sale::getRevenue).sum()));
        salesTotalProfitValue.setText(money(salesReportRows.stream().mapToDouble(Sale::getProfit).sum()));
    }

    private void resetSalesReportFilters() {
        salesFromDate.setValue(LocalDate.now().withDayOfMonth(1));
        salesToDate.setValue(LocalDate.now());
        salesSearchField.clear();
        refreshSalesReport();
    }

    private void applySalesDatePreset(String preset) {
        LocalDate today = LocalDate.now();
        switch (preset) {
            case "TODAY" -> {
                salesFromDate.setValue(today);
                salesToDate.setValue(today);
            }
            case "WEEK" -> {
                salesFromDate.setValue(today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
                salesToDate.setValue(today);
            }
            case "MONTH" -> {
                salesFromDate.setValue(today.withDayOfMonth(1));
                salesToDate.setValue(today);
            }
            default -> {
                return;
            }
        }
        refreshSalesReport();
    }

    private void refreshProfitReport() {
        LocalDate from = profitFromDate.getValue() == null ? LocalDate.now().withDayOfMonth(1) : profitFromDate.getValue();
        LocalDate to = profitToDate.getValue() == null ? LocalDate.now() : profitToDate.getValue();
        if (from.isAfter(to)) {
            to = from;
            profitToDate.setValue(to);
        }
        profitRows.setAll(reportService.profitReport(
            profitModeBox.getValue() == null ? "Daily" : profitModeBox.getValue(),
            from,
            to
        ));
        double totalProfit = profitRows.stream().mapToDouble(ProfitSummaryRow::profit).sum();
        double totalSales = reportService.salesReport(from, to, "").stream()
            .mapToDouble(Sale::getRevenue)
            .sum();
        profitTotalSalesValue.setText(money(totalSales));
        profitTotalProfitValue.setText(money(totalProfit));
        profitResultCountLabel.setText(profitRows.size() + " records");
    }

    private void resetProfitReportFilters() {
        profitModeBox.setValue("Daily");
        profitFromDate.setValue(LocalDate.now().withDayOfMonth(1));
        profitToDate.setValue(LocalDate.now());
        refreshProfitReport();
    }

    private void applyProfitDatePreset(String preset) {
        LocalDate today = LocalDate.now();
        switch (preset) {
            case "TODAY" -> {
                profitFromDate.setValue(today);
                profitToDate.setValue(today);
            }
            case "WEEK" -> {
                profitFromDate.setValue(today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
                profitToDate.setValue(today);
            }
            case "MONTH" -> {
                profitFromDate.setValue(today.withDayOfMonth(1));
                profitToDate.setValue(today);
            }
            default -> {
                return;
            }
        }
        refreshProfitReport();
    }

    private void refreshRestockReport() {
        restockRows.setAll(reportService.restockRecommendations());
        long urgentCount = restockRows.stream().filter(RestockRecommendation::needsReorder).count();
        double urgentCost = restockRows.stream()
            .filter(RestockRecommendation::needsReorder)
            .mapToDouble(RestockRecommendation::estimatedCost)
            .sum();
        restockSnapshotLabel.setText(urgentCount == 0
            ? "All products are trading above their minimum stock targets."
            : urgentCount + " product lines need attention. Estimated reorder budget: " + money(urgentCost));
    }

    private void refreshActivityFeed() {
        activityRows.setAll(activityLogService.recentActivity(120));
    }

    private void saveProduct() {
        clearProductValidationStyles();
        try {
            stageVariantEntryForSave();
            if (productNameField.getText() == null || productNameField.getText().isBlank()) {
                productNameField.getStyleClass().add("invalid-field");
                throw new IllegalArgumentException("Product name cannot be empty.");
            }
            String selectedCategory = selectedProductCategory();
            if (selectedCategory.isBlank()) {
                productCategoryBox.getStyleClass().add("invalid-field");
                throw new IllegalArgumentException("Category is required.");
            }
            if (productVariantRows.isEmpty()) {
                throw new IllegalArgumentException("Add at least one variant row before saving.");
            }

            List<Product> batch = productVariantRows.stream()
                .map(row -> row.toProduct(productNameField.getText().trim(), selectedCategory))
                .toList();
            inventoryService.saveProducts(batch);
            AlertUtil.info("Saved", batch.size() == 1
                ? "Product saved successfully."
                : "Product and variant rows saved successfully.");
            logActivity(
                "Product Saved",
                productNameField.getText().trim(),
                batch.size() + " variant row(s) saved in category " + selectedCategory + "."
            );
            clearProductForm();
            refreshAll();
        } catch (IllegalArgumentException exception) {
            if (selectedProductCategory().isBlank()) {
                productCategoryBox.getStyleClass().add("invalid-field");
            }
            AlertUtil.error("Validation Error", exception.getMessage());
        } catch (Exception exception) {
            AlertUtil.error("Save Failed", friendlyMessage(exception));
        }
    }

    private void addVariantRow() {
        clearVariantFieldValidationStyles();
        try {
            ProductVariantEntry entry = buildVariantEntry(null, true);
            ensureUniqueVariantEntry(entry, -1);
            productVariantRows.add(entry);
            clearVariantEditor();
        } catch (IllegalArgumentException exception) {
            AlertUtil.error("Variant Error", exception.getMessage());
        }
    }

    private void updateVariantRow() {
        clearVariantFieldValidationStyles();
        ProductVariantEntry selected = productVariantTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warning("No Selection", "Select a variant row to update.");
            return;
        }
        try {
            ProductVariantEntry entry = buildVariantEntry(selected, true);
            int selectedIndex = productVariantTable.getSelectionModel().getSelectedIndex();
            ensureUniqueVariantEntry(entry, selectedIndex);
            productVariantRows.set(selectedIndex, entry);
            clearVariantEditor();
        } catch (IllegalArgumentException exception) {
            AlertUtil.error("Variant Error", exception.getMessage());
        }
    }

    private void removeVariantRow() {
        ProductVariantEntry selected = productVariantTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warning("No Selection", "Select a variant row to remove.");
            return;
        }
        productVariantRows.remove(selected);
        clearVariantEditor();
    }

    private void deleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warning("No Selection", "Select a product to delete.");
            return;
        }
        try {
            inventoryService.deleteProduct(selected);
            AlertUtil.info("Deleted", "Product deleted successfully.");
            logActivity("Product Deleted", selected.getDisplayName(), "Removed from inventory before any sales history was attached.");
            clearProductForm();
            refreshAll();
        } catch (IllegalArgumentException exception) {
            AlertUtil.error("Delete Failed", exception.getMessage());
        } catch (Exception exception) {
            AlertUtil.error("Delete Failed", friendlyMessage(exception));
        }
    }

    private void addCategory() {
        categoryNameField.getStyleClass().remove("invalid-field");
        try {
            inventoryService.addCategory(categoryNameField.getText());
            logActivity("Category Added", categoryNameField.getText().trim(), "Created a new category.");
            categoryNameField.clear();
            AlertUtil.info("Category Added", "Category saved successfully.");
            refreshAll();
        } catch (IllegalArgumentException exception) {
            categoryNameField.getStyleClass().add("invalid-field");
            AlertUtil.error("Category Error", exception.getMessage());
        } catch (Exception exception) {
            AlertUtil.error("Category Error", friendlyMessage(exception));
        }
    }

    private void deleteCategory() {
        try {
            Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
            inventoryService.deleteCategory(selectedCategory);
            categoryTable.getSelectionModel().clearSelection();
            categoryNameField.clear();
            AlertUtil.info("Category Deleted", "Category removed successfully.");
            if (selectedCategory != null) {
                logActivity("Category Deleted", selectedCategory.categoryName(), "Removed unused category.");
            }
            refreshAll();
        } catch (IllegalArgumentException exception) {
            AlertUtil.error("Category Error", exception.getMessage());
        } catch (Exception exception) {
            AlertUtil.error("Category Error", friendlyMessage(exception));
        }
    }

    private void saveStaff() {
        staffUsernameField.getStyleClass().remove("invalid-field");
        staffRoleBox.getStyleClass().remove("invalid-field");
        clearStaffPasswordValidationStyles();
        try {
            User selectedStaff = staffTable.getSelectionModel().getSelectedItem();
            boolean creating = selectedStaff == null;
            String selectedRole = staffRoleBox.getValue();
            userService.saveStaff(selectedStaff, staffUsernameField.getText(), getStaffPasswordInput(), selectedRole);
            String username = normalizedText(staffUsernameField.getText());
            logActivity(
                creating ? "Staff Added" : "Staff Updated",
                username,
                creating
                    ? "Created a " + selectedRole + " workspace account."
                    : (normalizedText(getStaffPasswordInput()).isBlank()
                        ? "Updated account details to role " + selectedRole + " without changing the password."
                        : "Updated account details to role " + selectedRole + " and refreshed the password.")
            );
            if (!"Staff".equalsIgnoreCase(selectedRole)) {
                AlertUtil.info(
                    creating ? "Account Saved" : "Account Updated",
                    "Account saved successfully. Because the role is now " + selectedRole + ", it no longer appears in the Staff list."
                );
            } else {
                AlertUtil.info(
                    creating ? "Staff Added" : "Staff Updated",
                    creating ? "Staff account saved successfully." : "Staff account updated successfully."
                );
            }
            clearStaffForm();
            refreshStaffData();
        } catch (IllegalArgumentException exception) {
            if (normalizedText(staffUsernameField.getText()).isBlank()) {
                staffUsernameField.getStyleClass().add("invalid-field");
            }
            if (staffRoleBox.getValue() == null || staffRoleBox.getValue().isBlank()) {
                staffRoleBox.getStyleClass().add("invalid-field");
            }
            if (staffTable.getSelectionModel().getSelectedItem() == null && normalizedText(getStaffPasswordInput()).isBlank()) {
                markStaffPasswordInvalid();
            }
            AlertUtil.error("Staff Error", exception.getMessage());
        } catch (Exception exception) {
            AlertUtil.error("Staff Error", friendlyMessage(exception));
        }
    }

    private void deleteStaff() {
        User selectedStaff = staffTable.getSelectionModel().getSelectedItem();
        if (selectedStaff == null) {
            AlertUtil.warning("No Selection", "Select a staff account to delete.");
            return;
        }
        boolean confirmed = AlertUtil.confirm(
            "Delete Staff",
            "Delete the selected staff account for " + selectedStaff.getUsername() + "?"
        );
        if (!confirmed) {
            return;
        }
        try {
            userService.deleteStaff(selectedStaff, currentUser);
            logActivity("Staff Deleted", selectedStaff.getUsername(), "Removed staff account access.");
            AlertUtil.info("Staff Deleted", "Staff account removed successfully.");
            clearStaffForm();
            refreshStaffData();
        } catch (IllegalArgumentException exception) {
            AlertUtil.error("Staff Error", exception.getMessage());
        } catch (Exception exception) {
            AlertUtil.error("Staff Error", friendlyMessage(exception));
        }
    }

    private void recordSale() {
        saleQuantityField.getStyleClass().remove("invalid-field");
        try {
            Product product = saleProductBox.getValue();
            int quantity = parseInt(saleQuantityField, false);
            Sale sale = inventoryService.recordSale(product, quantity);
            int invoiceId = (int) (System.currentTimeMillis() % 100000);
            billPreview.setText(
                "Invoice #" + invoiceId + System.lineSeparator()
                    + "Product: " + sale.getProductName() + System.lineSeparator()
                    + "Quantity: " + sale.getQuantity() + System.lineSeparator()
                    + "Unit Price: " + money(sale.getRevenue() / sale.getQuantity()) + System.lineSeparator()
                    + "Total: " + money(sale.getRevenue()) + System.lineSeparator()
                    + "Profit: " + money(sale.getProfit())
            );
            logActivity("Sale Recorded", sale.getProductName(), "Quantity " + sale.getQuantity() + ", revenue " + money(sale.getRevenue()) + ".");
            clearSaleForm(false);
            AlertUtil.info("Sale Recorded", "Sale saved and stock updated successfully.");
            refreshAll();
        } catch (IllegalArgumentException exception) {
            saleQuantityField.getStyleClass().add("invalid-field");
            AlertUtil.error("Sale Error", exception.getMessage());
        } catch (Exception exception) {
            AlertUtil.error("Sale Failed", friendlyMessage(exception));
        }
    }

    private void updateSale() {
        saleQuantityField.getStyleClass().remove("invalid-field");
        try {
            Product product = saleProductBox.getValue();
            int quantity = parseInt(saleQuantityField, false);
            Sale sale = inventoryService.updateSale(editingSale, product, quantity);
            billPreview.setText(
                "Edited Sale" + System.lineSeparator()
                    + "Product: " + sale.getProductName() + System.lineSeparator()
                    + "Quantity: " + sale.getQuantity() + System.lineSeparator()
                    + "Unit Price: " + money(sale.getRevenue() / sale.getQuantity()) + System.lineSeparator()
                    + "Total: " + money(sale.getRevenue()) + System.lineSeparator()
                    + "Profit: " + money(sale.getProfit())
            );
            logActivity("Sale Updated", sale.getProductName(), "Quantity updated to " + sale.getQuantity() + ".");
            clearSaleForm(false);
            AlertUtil.info("Sale Updated", "The selected sale was updated and stock was adjusted.");
            refreshAll();
        } catch (IllegalArgumentException exception) {
            saleQuantityField.getStyleClass().add("invalid-field");
            AlertUtil.error("Sale Error", exception.getMessage());
        } catch (Exception exception) {
            AlertUtil.error("Update Failed", friendlyMessage(exception));
        }
    }

    private void startEditingSelectedSale() {
        Sale selectedSale = salesHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            AlertUtil.warning("No Selection", "Select a sale from the list to edit.");
            return;
        }
        populateSaleForm(selectedSale);
    }

    private void deleteSelectedSale() {
        Sale selectedSale = salesHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            AlertUtil.warning("No Selection", "Select a sale from the list to remove.");
            return;
        }
        boolean confirmed = AlertUtil.confirm(
            "Remove Sale",
            "Remove the selected sale and restore its quantity back to stock?"
        );
        if (!confirmed) {
            return;
        }
        try {
            inventoryService.removeSale(selectedSale);
            clearSaleForm();
            AlertUtil.info("Sale Removed", "The selected sale was removed and stock was restored.");
            logActivity("Sale Removed", selectedSale.getProductName(), "Removed sale #" + selectedSale.getSaleId() + " and restored stock.");
            refreshAll();
        } catch (Exception exception) {
            AlertUtil.error("Remove Failed", friendlyMessage(exception));
        }
    }

    private void populateSaleForm(Sale sale) {
        editingSale = sale;
        if (sale == null) {
            setSaleEditMode(false);
            return;
        }
        setSaleEditMode(true);

        Product matchingProduct = inventoryService.getProducts("", "All").stream()
            .filter(product -> product.getProductId() == sale.getProductId())
            .findFirst()
            .orElse(null);
        if (matchingProduct != null) {
            saleProductSearchField.setText(matchingProduct.getDisplayName());
            refreshSalesChoices();
        }
        saleProductBox.setValue(matchingProduct);
        saleQuantityField.setText(String.valueOf(sale.getQuantity()));
        billPreview.setText(
            "Editing Sale #" + sale.getSaleId() + System.lineSeparator()
                + "Date: " + sale.getSaleDate() + System.lineSeparator()
                + "Product: " + sale.getProductName() + System.lineSeparator()
                + "Quantity: " + sale.getQuantity() + System.lineSeparator()
                + "Revenue: " + money(sale.getRevenue()) + System.lineSeparator()
                + "Profit: " + money(sale.getProfit())
        );
    }

    private void populateProductForm(Product product) {
        if (product == null) {
            return;
        }
        productFormModeLabel.setText("Edit Product");
        productFormHintLabel.setText("Update the details you need, then save the product.");
        productNameField.setText(product.getProductName());
        productCategoryBox.setValue(product.getCategory());
        productVariantRows.setAll(ProductVariantEntry.fromProduct(product));
        productVariantTable.getSelectionModel().selectFirst();
        clearProductValidationStyles();
    }

    private void populateStaffForm(User user) {
        if (user == null) {
            clearStaffFormFields();
            staffFormModeLabel.setText("New Staff Account");
            staffFormHintLabel.setText("Create staff login access for day-to-day inventory and sales work.");
            return;
        }
        staffFormModeLabel.setText("Edit Staff Account");
        staffFormHintLabel.setText("Update the username or enter a new password. Leave password blank to keep the current one.");
        staffUsernameField.setText(user.getUsername());
        staffRoleBox.setValue(user.getRole());
        staffPasswordField.clear();
        staffShowPasswordCheck.setSelected(false);
        staffUsernameField.getStyleClass().remove("invalid-field");
        staffRoleBox.getStyleClass().remove("invalid-field");
        clearStaffPasswordValidationStyles();
    }

    private void clearProductForm() {
        productTable.getSelectionModel().clearSelection();
        productNameField.clear();
        productCategoryBox.setValue(null);
        productVariantRows.clear();
        productFormModeLabel.setText("New Product");
        productFormHintLabel.setText("Choose a category, fill the stock details, add one row, then save.");
        clearVariantEditor();
        clearProductValidationStyles();
    }

    private void clearStaffForm() {
        staffTable.getSelectionModel().clearSelection();
        clearStaffFormFields();
        staffFormModeLabel.setText("New Staff Account");
        staffFormHintLabel.setText("Create staff login access for day-to-day inventory and sales work.");
    }

    private void clearStaffFormFields() {
        staffUsernameField.clear();
        staffRoleBox.setValue("Staff");
        staffPasswordField.clear();
        staffShowPasswordCheck.setSelected(false);
        staffUsernameField.getStyleClass().remove("invalid-field");
        staffRoleBox.getStyleClass().remove("invalid-field");
        clearStaffPasswordValidationStyles();
    }

    private void populateVariantEditor(ProductVariantEntry entry) {
        if (entry == null) {
            clearVariantEntryFields();
            return;
        }
        variantField.setText(entry.getVariant());
        colorField.setText(entry.getColor());
        costPriceField.setText(String.valueOf(entry.getCostPrice()));
        sellingPriceField.setText(String.valueOf(entry.getSellingPrice()));
        quantityField.setText(String.valueOf(entry.getQuantity()));
        minimumStockField.setText(String.valueOf(entry.getMinimumStock()));
        clearVariantFieldValidationStyles();
    }

    private void clearVariantEditor() {
        productVariantTable.getSelectionModel().clearSelection();
        clearVariantEntryFields();
        updateVariantActionState();
    }

    private void clearVariantEntryFields() {
        variantField.clear();
        colorField.clear();
        costPriceField.clear();
        sellingPriceField.clear();
        quantityField.clear();
        minimumStockField.clear();
        clearVariantFieldValidationStyles();
    }

    private void clearVariantFieldValidationStyles() {
        variantField.getStyleClass().remove("invalid-field");
        colorField.getStyleClass().remove("invalid-field");
        costPriceField.getStyleClass().remove("invalid-field");
        sellingPriceField.getStyleClass().remove("invalid-field");
        quantityField.getStyleClass().remove("invalid-field");
        minimumStockField.getStyleClass().remove("invalid-field");
    }

    private void updateVariantActionState() {
        boolean hasSelection = productVariantTable.getSelectionModel().getSelectedItem() != null;
        updateVariantButton.setDisable(!hasSelection);
        removeVariantButton.setDisable(!hasSelection);
    }

    private ProductVariantEntry buildVariantEntry(ProductVariantEntry selected, boolean markFields) {
        return new ProductVariantEntry(
            selected == null ? 0 : selected.getProductId(),
            normalizedText(variantField.getText()),
            normalizedText(colorField.getText()),
            parseDouble(costPriceField, markFields),
            parseDouble(sellingPriceField, markFields),
            parseInt(quantityField, markFields),
            parseInt(minimumStockField, markFields)
        );
    }

    private void stageVariantEntryForSave() {
        if (!hasVariantEditorInput()) {
            return;
        }
        ProductVariantEntry selected = productVariantTable.getSelectionModel().getSelectedItem();
        ProductVariantEntry entry = buildVariantEntry(selected, true);
        int selectedIndex = productVariantTable.getSelectionModel().getSelectedIndex();
        ensureUniqueVariantEntry(entry, selectedIndex);
        if (selected == null) {
            productVariantRows.add(entry);
        } else {
            productVariantRows.set(selectedIndex, entry);
        }
        clearVariantEditor();
    }

    private boolean hasVariantEditorInput() {
        return hasText(variantField.getText())
            || hasText(colorField.getText())
            || hasText(costPriceField.getText())
            || hasText(sellingPriceField.getText())
            || hasText(quantityField.getText())
            || hasText(minimumStockField.getText());
    }

    private void ensureUniqueVariantEntry(ProductVariantEntry candidate, int ignoreIndex) {
        String candidateVariant = normalizedKey(candidate.getVariant());
        String candidateColor = normalizedKey(candidate.getColor());
        for (int index = 0; index < productVariantRows.size(); index++) {
            if (index == ignoreIndex) {
                continue;
            }
            ProductVariantEntry existing = productVariantRows.get(index);
            if (candidateVariant.equals(normalizedKey(existing.getVariant()))
                && candidateColor.equals(normalizedKey(existing.getColor()))) {
                throw new IllegalArgumentException("Duplicate variant/color rows are not allowed.");
            }
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizedText(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizedKey(String value) {
        return normalizedText(value).toLowerCase(Locale.ROOT);
    }

    private void clearSaleForm() {
        clearSaleForm(true);
    }

    private void clearSaleForm(boolean clearBillPreview) {
        editingSale = null;
        saleProductSearchField.clear();
        refreshSalesChoices();
        saleProductBox.setValue(null);
        saleQuantityField.clear();
        saleQuantityField.getStyleClass().remove("invalid-field");
        setSaleEditMode(false);
        if (salesHistoryTable.getSelectionModel().getSelectedItem() != null) {
            salesHistoryTable.getSelectionModel().clearSelection();
        }
        if (clearBillPreview) {
            billPreview.clear();
        }
        updateSaleSelectionState();
    }

    private void exportSalesReport() {
        List<String> rows = new ArrayList<>();
        rows.add("Date,Product Name,Quantity Sold,Revenue,Profit");
        salesReportRows.forEach(sale -> rows.add(csv(
            sale.getSaleDate().toString(),
            sale.getProductName(),
            String.valueOf(sale.getQuantity()),
            moneyRaw(sale.getRevenue()),
            moneyRaw(sale.getProfit())
        )));
        export("sales-report.csv", rows);
    }

    private void exportProfitReport() {
        List<String> rows = new ArrayList<>();
        rows.add("Period,Profit");
        profitRows.forEach(row -> rows.add(csv(row.period(), moneyRaw(row.profit()))));
        export("profit-report.csv", rows);
    }

    private void exportRestockReport() {
        List<String> rows = new ArrayList<>();
        rows.add("Product,Category,Current Stock,Minimum Stock,Suggested Order,Urgency,Estimated Cost,Projected Revenue,Projected Profit");
        restockRows.forEach(row -> rows.add(csv(
            row.productLabel(),
            row.category(),
            String.valueOf(row.currentStock()),
            String.valueOf(row.minimumStock()),
            String.valueOf(row.suggestedOrder()),
            row.urgency(),
            moneyRaw(row.estimatedCost()),
            moneyRaw(row.projectedRevenue()),
            moneyRaw(row.projectedProfit())
        )));
        export("restock-planner.csv", rows);
    }

    private void exportActivityReport() {
        List<String> rows = new ArrayList<>();
        rows.add("Time,User,Action,Subject,Details");
        activityRows.forEach(row -> rows.add(csv(
            row.formattedTimestamp(),
            row.performedBy(),
            row.actionType(),
            row.subject(),
            row.details()
        )));
        export("activity-log.csv", rows);
    }

    private void backupDatabase() {
        try {
            exportService.exportDatabaseBackup(
                view.getRoot().getScene().getWindow(),
                DatabaseConnection.getInstance().getDatabaseName() + "-backup-" + LocalDate.now() + ".sql"
            );
            AlertUtil.info("Backup Complete", "Database backup saved successfully.");
        } catch (IOException exception) {
            AlertUtil.error("Backup Failed", "Unable to save the database backup.");
        }
    }

    private void export(String fileName, List<String> rows) {
        try {
            exportService.exportCsv(view.getRoot().getScene().getWindow(), fileName, rows);
            AlertUtil.info("Export Complete", "Report exported successfully.");
        } catch (IOException exception) {
            AlertUtil.error("Export Failed", "Unable to export CSV file.");
        }
    }

    private VBox wrapPanel(String title, javafx.scene.Node content) {
        Label heading = new Label(title);
        heading.getStyleClass().add("panel-title");
        VBox box = new VBox(14, heading, content);
        box.getStyleClass().add("panel");
        VBox.setVgrow(content, Priority.ALWAYS);
        if (content instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        return box;
    }

    private VBox metricCard(String iconPath, String title, Label value, String styleClass) {
        return metricCard(iconPath, new Label(title), value, styleClass);
    }

    private VBox metricCard(String iconPath, Label title, Label value, String styleClass) {
        ImageView icon = imageIcon(iconPath, 26);
        icon.getStyleClass().add("metric-image");
        StackPane iconHolder = new StackPane(icon);
        iconHolder.getStyleClass().add("metric-icon");
        Label heading = title;
        heading.getStyleClass().add("card-title");
        value.getStyleClass().add("card-value");
        VBox textBox = new VBox(8, heading, value);
        HBox card = new HBox(14, iconHolder, textBox);
        card.getStyleClass().addAll("metric-card", styleClass);
        card.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(card, Priority.ALWAYS);
        return new VBox(card);
    }

    private FlowPane toolbar(javafx.scene.Node... nodes) {
        FlowPane bar = new FlowPane();
        bar.setHgap(10);
        bar.setVgap(10);
        bar.getStyleClass().add("toolbar-row");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getChildren().addAll(nodes);
        return bar;
    }

    private FlowPane actionWrap(Button... buttons) {
        FlowPane actionRow = new FlowPane();
        actionRow.setHgap(10);
        actionRow.setVgap(10);
        actionRow.getStyleClass().add("action-wrap");
        for (Button button : buttons) {
            if (!button.getStyleClass().contains("app-action-button")) {
                button.getStyleClass().add("app-action-button");
            }
        }
        actionRow.getChildren().addAll(buttons);
        return actionRow;
    }

    private VBox labeledField(String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        VBox box = new VBox(6, label, field);
        VBox.setVgrow(field, Priority.NEVER);
        return box;
    }

    private Tab reportTab(String title, Parent content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }

    private FlowPane reportSummaryStrip(VBox... cards) {
        FlowPane row = new FlowPane();
        row.setHgap(18);
        row.setVgap(18);
        row.getStyleClass().add("report-summary-strip");
        row.getChildren().addAll(cards);
        return row;
    }

    private VBox reportSummaryCard(String title, Label value) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("report-summary-label");
        value.getStyleClass().add("report-summary-value");
        VBox card = new VBox(10, titleLabel, value);
        card.getStyleClass().add("report-summary-card");
        card.setMinWidth(220);
        return card;
    }

    private Parent accessPanel(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("empty-state-title");
        VBox box = new VBox(label);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().addAll("panel", "empty-state");
        box.setPrefHeight(420);
        return box;
    }

    private void configureDashboardTransactionsTable() {
        dashboardTransactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        addColumn(dashboardTransactionsTable, "Date", sale -> sale.getSaleDate().toString());
        addColumn(dashboardTransactionsTable, "Product", Sale::getProductName);
        addColumn(dashboardTransactionsTable, "Qty", Sale::getQuantity);
        addColumn(dashboardTransactionsTable, "Sale Price", sale -> money(sale.getRevenue()));
        addColumn(dashboardTransactionsTable, "Profit", sale -> money(sale.getProfit()));
    }

    private void retainSelection(ComboBox<String> comboBox, List<String> values, String fallback) {
        String current = comboBox.getValue();
        String targetValue = values.contains(current) ? current : fallback;
        comboBox.getItems().setAll(values);
        if (!Objects.equals(comboBox.getValue(), targetValue)) {
            comboBox.setValue(targetValue);
        }
    }

    private String selectedOrAll(ComboBox<String> comboBox) {
        return comboBox.getValue() == null ? "All" : comboBox.getValue();
    }

    private String blankLabel(String value) {
        return hasText(value) ? value : "-";
    }

    private String productLabel(Product product) {
        return product == null ? "" : product.getDisplayName() + " (Stock: " + product.getQuantity() + ")";
    }

    private String selectedProductCategory() {
        return normalizedText(productCategoryBox.getValue());
    }

    private <T> void addColumn(TableView<T> table, String title, Function<T, Object> mapper) {
        TableColumn<T, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(mapper.apply(cell.getValue())));
        table.getColumns().add(column);
    }

    private boolean hasManagementAccess() {
        return currentUser != null
            && "Admin".equalsIgnoreCase(currentUser.getRole());
    }

    private boolean hasReportAccess() {
        return currentUser != null
            && "Admin".equalsIgnoreCase(currentUser.getRole());
    }

    private String getStaffPasswordInput() {
        return staffShowPasswordCheck.isSelected() ? staffPasswordVisibleField.getText() : staffPasswordField.getText();
    }

    private void clearStaffPasswordValidationStyles() {
        staffPasswordField.getStyleClass().remove("invalid-field");
        staffPasswordVisibleField.getStyleClass().remove("invalid-field");
    }

    private void markStaffPasswordInvalid() {
        if (!staffPasswordField.getStyleClass().contains("invalid-field")) {
            staffPasswordField.getStyleClass().add("invalid-field");
        }
        if (!staffPasswordVisibleField.getStyleClass().contains("invalid-field")) {
            staffPasswordVisibleField.getStyleClass().add("invalid-field");
        }
    }

    private ImageView imageIcon(String resourcePath, double size) {
        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(resourcePath))));
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private String money(double amount) {
        return "Rs " + currencyFormat.format(amount);
    }

    private String moneyRaw(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }

    private String csv(String... values) {
        List<String> escaped = new ArrayList<>();
        for (String value : values) {
            String safeValue = value == null ? "" : value;
            escaped.add('"' + safeValue.replace("\"", "\"\"") + '"');
        }
        return String.join(",", escaped);
    }

    private double parseDouble(TextField field, boolean markField) {
        try {
            if (markField) {
                field.getStyleClass().remove("invalid-field");
            }
            return Double.parseDouble(field.getText().trim());
        } catch (Exception exception) {
            if (markField && !field.getStyleClass().contains("invalid-field")) {
                field.getStyleClass().add("invalid-field");
            }
            throw new IllegalArgumentException("Enter a valid number.");
        }
    }

    private int parseInt(TextField field, boolean markField) {
        try {
            if (markField) {
                field.getStyleClass().remove("invalid-field");
            }
            return Integer.parseInt(field.getText().trim());
        } catch (Exception exception) {
            if (markField && !field.getStyleClass().contains("invalid-field")) {
                field.getStyleClass().add("invalid-field");
            }
            throw new IllegalArgumentException("Enter a valid whole number.");
        }
    }

    private void clearProductValidationStyles() {
        productNameField.getStyleClass().remove("invalid-field");
        variantField.getStyleClass().remove("invalid-field");
        colorField.getStyleClass().remove("invalid-field");
        productCategoryBox.getStyleClass().remove("invalid-field");
        costPriceField.getStyleClass().remove("invalid-field");
        sellingPriceField.getStyleClass().remove("invalid-field");
        quantityField.getStyleClass().remove("invalid-field");
        minimumStockField.getStyleClass().remove("invalid-field");
    }

    private String friendlyMessage(Exception exception) {
        return exception.getMessage() == null ? "Something went wrong. Please try again." : exception.getMessage();
    }

    private void logActivity(String actionType, String subject, String details) {
        activityLogService.log(currentUser, actionType, subject, details);
    }

    private void setSaleEditMode(boolean editing) {
        recordSaleButton.setManaged(!editing);
        recordSaleButton.setVisible(!editing);
        clearSaleButton.setManaged(!editing);
        clearSaleButton.setVisible(!editing);
        updateSaleButton.setManaged(editing);
        updateSaleButton.setVisible(editing);
        cancelSaleEditButton.setManaged(editing);
        cancelSaleEditButton.setVisible(editing);
    }

    private void updateSaleSelectionState() {
        boolean hasSelection = salesHistoryTable.getSelectionModel().getSelectedItem() != null;
        editSaleButton.setDisable(!hasSelection);
        removeSaleButton.setDisable(!hasSelection);
    }
}
