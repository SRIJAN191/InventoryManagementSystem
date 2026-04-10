package com.ims.service;

import com.ims.dao.ProductDAO;
import com.ims.dao.ReportDAO;
import com.ims.dao.SaleDAO;
import com.ims.model.DashboardMetrics;
import com.ims.model.Product;
import com.ims.model.ProfitSummaryRow;
import com.ims.model.Sale;

import java.time.LocalDate;
import java.util.List;

public class DashboardService {
    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final ReportDAO reportDAO = new ReportDAO();

    public DashboardMetrics getMetrics() {
        List<Product> products = productDAO.findAll("", "All");
        int totalProducts = products.size();
        double totalStockValue = products.stream()
            .mapToDouble(Product::getStockValue)
            .sum();
        double todaysSales = saleDAO.sumRevenueBetween(LocalDate.now(), LocalDate.now());
        double todaysProfit = saleDAO.sumProfitBetween(LocalDate.now(), LocalDate.now());
        int lowStockCount = (int) products.stream()
            .filter(Product::isBelowMinimumStock)
            .count();
        return new DashboardMetrics(totalProducts, totalStockValue, todaysSales, todaysProfit, lowStockCount);
    }

    public List<ProfitSummaryRow> monthlyProfit() {
        return saleDAO.monthlyProfit();
    }

    public List<ProfitSummaryRow> categorySales() {
        return reportDAO.categorySales(LocalDate.now().withDayOfMonth(1), LocalDate.now());
    }

    public List<Product> lowStockProducts() {
        return productDAO.findAll("", "All").stream()
            .filter(Product::isBelowMinimumStock)
            .toList();
    }

    public List<Sale> recentSales(int limit) {
        return saleDAO.findRecent(limit);
    }
}
