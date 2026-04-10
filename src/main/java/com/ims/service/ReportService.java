package com.ims.service;

import com.ims.dao.ReportDAO;
import com.ims.dao.SaleDAO;
import com.ims.model.InventoryReportRow;
import com.ims.model.ProfitSummaryRow;
import com.ims.model.RestockRecommendation;
import com.ims.model.Sale;

import java.time.LocalDate;
import java.util.List;

public class ReportService {
    private final ReportDAO reportDAO = new ReportDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    public List<Sale> salesReport(LocalDate from, LocalDate to, String search) {
        return saleDAO.findSales(from, to, search);
    }

    public List<InventoryReportRow> inventoryReport(String search, String category, String sortBy) {
        return reportDAO.inventoryReport(search, category, sortBy);
    }

    public List<ProfitSummaryRow> profitReport(String mode, LocalDate from, LocalDate to) {
        return reportDAO.profitReport(mode, from, to);
    }

    public List<RestockRecommendation> restockRecommendations() {
        return reportDAO.restockRecommendations();
    }
}
