package com.gymapp.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinStatsResponse {
    /** Tổng số buổi check-in (tổng buổi tập) */
    private long totalSessions;

    /** Số ngày liên tiếp check-in hiện tại (streak) */
    private int streakDays;

    /** Ước tính tổng giờ tập (mỗi buổi tính 1.5h) */
    private double totalHours;
}
