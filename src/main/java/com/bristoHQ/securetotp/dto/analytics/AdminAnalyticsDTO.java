package com.bristoHQ.securetotp.dto.analytics;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminAnalyticsDTO {
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long premiumUsers;
    private Long verifiedUsers;
    private Long totalViews;
    private Long todayViews;
    private Long thisWeekViews;
    private Map<String, Long> usersByRole;
    private Map<String, Long> userRegistrationTrend;
    private Map<String, Long> dailyActiveUsers;
}
