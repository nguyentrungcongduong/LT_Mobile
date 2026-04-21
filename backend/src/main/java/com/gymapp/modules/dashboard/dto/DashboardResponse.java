package com.gymapp.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalUsers;
    private long activeMembers;
    private BigDecimal monthlyRevenue;
    private long todayBookings;
    private long todayCheckins;
    private List<RevenuePoint> revenueLast7Days;
    private List<TopPt> topPTs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenuePoint {
        private String date;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPt {
        private String name;
        private BigDecimal revenue;
    }
}
