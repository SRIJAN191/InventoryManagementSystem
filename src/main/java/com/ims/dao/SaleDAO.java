package com.ims.dao;

import com.ims.db.DatabaseConnection;
import com.ims.model.ProfitSummaryRow;
import com.ims.model.Sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {
    public List<Sale> findSales(LocalDate from, LocalDate to, String search) {
        String sql = """
            SELECT * FROM sales
            WHERE sale_date BETWEEN ? AND ?
              AND (? = '' OR lower(product_name) LIKE lower(?))
            ORDER BY sale_date DESC, sale_id DESC
            """;
        List<Sale> sales = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, from.toString());
            statement.setString(2, to.toString());
            statement.setString(3, search == null ? "" : search.trim());
            statement.setString(4, "%" + (search == null ? "" : search.trim()) + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                sales.add(map(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to fetch sales", exception);
        }
        return sales;
    }

    public List<Sale> findRecent(int limit) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC, sale_id DESC LIMIT ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                sales.add(map(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to fetch recent sales", exception);
        }
        return sales;
    }

    public double sumProfitBetween(LocalDate from, LocalDate to) {
        return querySingleDouble("""
            SELECT COALESCE(SUM(profit), 0) AS total_profit
            FROM sales
            WHERE sale_date BETWEEN ? AND ?
            """, from, to);
    }

    public double sumRevenueBetween(LocalDate from, LocalDate to) {
        return querySingleDouble("""
            SELECT COALESCE(SUM(revenue), 0) AS total_revenue
            FROM sales
            WHERE sale_date BETWEEN ? AND ?
            """, from, to);
    }

    public List<ProfitSummaryRow> monthlyProfit() {
        List<ProfitSummaryRow> rows = new ArrayList<>();
        String sql = """
            SELECT substr(sale_date, 1, 7) AS period, COALESCE(SUM(profit), 0) AS total_profit
            FROM sales
            GROUP BY substr(sale_date, 1, 7)
            ORDER BY period
            """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(new ProfitSummaryRow(
                    resultSet.getString("period"),
                    resultSet.getDouble("total_profit")
                ));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load monthly profit", exception);
        }
        return rows;
    }

    public boolean existsForProduct(int productId) {
        String sql = "SELECT 1 FROM sales WHERE product_id = ? LIMIT 1";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to verify product sales history", exception);
        }
    }

    private double querySingleDouble(String sql, LocalDate from, LocalDate to) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, from.toString());
            statement.setString(2, to.toString());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() ? resultSet.getDouble(1) : 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to aggregate sales data", exception);
        }
    }

    private Sale map(ResultSet resultSet) throws SQLException {
        return new Sale(
            resultSet.getInt("sale_id"),
            resultSet.getInt("product_id"),
            resultSet.getString("product_name"),
            resultSet.getInt("quantity"),
            resultSet.getDouble("revenue"),
            resultSet.getDouble("profit"),
            LocalDate.parse(resultSet.getString("sale_date"))
        );
    }
}
