package com.helpdesk.service;

import java.time.LocalDate;
import java.util.Map;

public interface ReportService {
    Map<String, Object> getDashboardStats(Long companyId, Long userId, String role);
    byte[] exportPdfReport(String type, Long companyId, LocalDate startDate, LocalDate endDate);
    byte[] exportExcelReport(String type, Long companyId, LocalDate startDate, LocalDate endDate);
}
