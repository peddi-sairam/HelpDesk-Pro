package com.helpdesk.service.impl;

import com.helpdesk.service.ReportService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Override
    public Map<String, Object> getDashboardStats(Long companyId, Long userId, String role) {
        return new HashMap<>();
    }

    @Override
    public byte[] exportPdfReport(String type, Long companyId, LocalDate startDate, LocalDate endDate) {
        return new byte[0];
    }

    @Override
    public byte[] exportExcelReport(String type, Long companyId, LocalDate startDate, LocalDate endDate) {
        return new byte[0];
    }
}
