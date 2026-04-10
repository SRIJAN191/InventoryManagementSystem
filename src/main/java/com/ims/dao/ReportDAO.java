package com.ims.dao;

import com.ims.db.DatabaseConnection;
import com.ims.model.InventoryReportRow;
import com.ims.model.ProfitSummaryRow;
import com.ims.model.RestockRecommendation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {
    public List<InventoryReportRow> inventoryReport(String search, String category, String sortBy) {
        String order = switch (sortBy) {
            case "Stock" -> "quantity DESC";
            case "Value" -> "(cost_price * quantity) DESC";
            default -> "product_name ASC, COALESCE(variant, '') ASC, COALESCE(color, '') ASC";
        };
        String sql = """
            SELECT
                TRIM(CONCAT(
                    product_name,
                    CASE WHEN COALESCE(variant, '') = '' THEN '' ELSE CONCAT(' - ', variant) END,
                    CASE WHEN COALESCE(color, '') = '' THEN '' ELSE CONCAT(' - ', color) END
                )) AS product_label,
                quantity,
                cost_price,
                (cost_price * quantity) AS total_value
            FROM products
            WHERE (? = '' OR lower(product_name) LIKE lower(?)
                          OR lower(COALESCE(variant, '')) LIKE lower(?)
                          OR lower(COALESCE(color, '')) LIKE lower(?))
              AND (? = 'All' OR category = ?)
            ORDER BY %s
            """.formatted(order);
        List<InventoryReportRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String normalizedSearch = search == null ? "" : search.trim();
            String searchPattern = "%" + normalizedSearch + "%";
            statement.setString(1, normalizedSearch);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            statement.setString(4, searchPattern);
            statement.setString(5, category == null ? "All" : category);
            statement.setString(6, category == null ? "All" : category);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                rows.add(new InventoryReportRow(
                    resultSet.getString("product_label"),
                    resultSet.getInt("quantity"),
                    resultSet.getDouble("cost_price"),
                    resultSet.getDouble("total_value")
                ));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to generate inventory report", exception);
        }
        return rows;
    }

    public List<ProfitSummaryRow> profitReport(String mode, LocalDate from, LocalDate to) {
        String sql = switch (mode) {
            case "Weekly" -> """
                SELECT DATE_FORMAT(sale_date, '%x-W%v') AS period, COALESCE(SUM(profit), 0) AS total_profit
                FROM sales
                WHERE sale_date BETWEEN ? AND ?
                GROUP BY DATE_FORMAT(sale_date, '%x-W%v')
                ORDER BY period DESC
                """;
            case "Monthly" -> """
                SELECT DATE_FORMAT(sale_date, '%Y-%m') AS period, COALESCE(SUM(profit), 0) AS total_profit
                FROM sales
                WHERE sale_date BETWEEN ? AND ?
                GROUP BY DATE_FORMAT(sale_date, '%Y-%m')
                ORDER BY period DESC
                """;
            default -> """
                SELECT sale_date AS period, COALESCE(SUM(profit), 0) AS total_profit
                FROM sales
                WHERE sale_date BETWEEN ? AND ?
                GROUP BY sale_date
                ORDER BY period DESC
                """;
        };
        List<ProfitSummaryRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, from.toString());
            statement.setString(2, to.toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                rows.add(new ProfitSummaryRow(
                    resultSet.getString("period"),
                    resultSet.getDouble("total_profit")
                ));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to generate profit report", exception);
        }
        return rows;
    }

    public List<ProfitSummaryRow> categorySales(LocalDate from, LocalDate to) {
        String sql = """
            SELECT p.category AS period, COALESCE(SUM(s.revenue), 0) AS total_revenue
            FROM sales s
            JOIN products p ON s.product_id = p.product_id
            WHERE s.sale_date BETWEEN ? AND ?
            GROUP BY p.category
            ORDER BY total_revenue DESC
            """;
        List<ProfitSummaryRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, from.toString());
            statement.setString(2, to.toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                rows.add(new ProfitSummaryRow(
                    resultSet.getString("period"),
                    resultSet.getDouble("total_revenue")
                ));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load category sales", exception);
        }
        return rows;
    }

    public List<RestockRecommendation> restockRecommendations() {
        String sql = """
            SELECT
                TRIM(CONCAT(
                    product_name,
                    CASE WHEN COALESCE(variant, '') = '' THEN '' ELSE CONCAT(' - ', variant) END,
                    CASE WHEN COALESCE(color, '') = '' THEN '' ELSE CONCAT(' - ', color) END
                )) AS product_label,
                category,
                quantity,
                minimum_stock,
                CASE
                    WHEN quantity = 0 THEN 'Critical'
                    WHEN quantity < minimum_stock THEN 'High'
                    WHEN quantity = minimum_stock THEN 'Monitor'
                    ELSE 'Healthy'
                END AS urgency,
                CASE
                    WHEN quantity <= minimum_stock THEN ((minimum_stock * 2) - quantity)
                    ELSE 0
                END AS suggested_order,
                CASE
                    WHEN quantity <= minimum_stock THEN cost_price * ((minimum_stock * 2) - quantity)
                    ELSE 0
                END AS estimated_cost,
                CASE
                    WHEN quantity <= minimum_stock THEN selling_price * ((minimum_stock * 2) - quantity)
                    ELSE 0
                END AS projected_revenue,
                CASE
                    WHEN quantity <= minimum_stock THEN (selling_price - cost_price) * ((minimum_stock * 2) - quantity)
                    ELSE 0
                END AS projected_profit
            FROM products
            ORDER BY
                CASE
                    WHEN quantity = 0 THEN 0
                    WHEN quantity < minimum_stock THEN 1
                    WHEN quantity = minimum_stock THEN 2
                    ELSE 3
                END,
                (minimum_stock - quantity) DESC,
                product_name ASC
            """;
        List<RestockRecommendation> rows = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(new RestockRecommendation(
                    resultSet.getString("product_label"),
                    resultSet.getString("category"),
                    resultSet.getInt("quantity"),
                    resultSet.getInt("minimum_stock"),
                    resultSet.getInt("suggested_order"),
                    resultSet.getString("urgency"),
                    resultSet.getDouble("estimated_cost"),
                    resultSet.getDouble("projected_revenue"),
                    resultSet.getDouble("projected_profit")
                ));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to build restock planner", exception);
        }
        return rows;
    }
}
