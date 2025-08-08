package com.bristoHQ.securetotp.services.analytics;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bristoHQ.securetotp.dto.analytics.AdminAnalyticsDTO;
import com.bristoHQ.securetotp.models.analytics.PageView;
import com.bristoHQ.securetotp.models.role.RoleName;
import com.bristoHQ.securetotp.repositories.UserRepository;
import com.bristoHQ.securetotp.repositories.analytics.PageViewRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final PageViewRepository pageViewRepository;

    public AdminAnalyticsDTO getAdminAnalytics() {
        AdminAnalyticsDTO analytics = new AdminAnalyticsDTO();

        try {
            // Total users
            analytics.setTotalUsers(userRepository.count());

            // Active users (users who have been active in the last 30 days)
            Date thirtyDaysAgo = getDateDaysAgo(30);
            analytics.setActiveUsers(userRepository.countByLastActiveAtAfter(thirtyDaysAgo));

            // New users today
            Date today = getTodayStart();
            analytics.setNewUsersToday(userRepository.countByAccountCreatedAtAfter(today));

            // New users this week
            Date weekStart = getDateDaysAgo(7);
            analytics.setNewUsersThisWeek(userRepository.countByAccountCreatedAtAfter(weekStart));

            // Premium users
            analytics.setPremiumUsers(userRepository.countByIsPremium(true));

            // Verified users
            analytics.setVerifiedUsers(userRepository.countByVerified(true));

            // Total views
            analytics.setTotalViews(pageViewRepository.count());

            // Today views
            analytics.setTodayViews(pageViewRepository.countByTimestampAfter(today));

            // This week views
            analytics.setThisWeekViews(pageViewRepository.countByTimestampAfter(weekStart));

            // Users by role
            Map<String, Long> usersByRole = new HashMap<>();
            usersByRole.put("USER", userRepository.countByRolesRoleName(RoleName.USER));
            usersByRole.put("ADMIN", userRepository.countByRolesRoleName(RoleName.ADMIN));
            usersByRole.put("SUPER_ADMIN", userRepository.countByRolesRoleName(RoleName.SUPERADMIN));
            analytics.setUsersByRole(usersByRole);

            // User registration trend (last 7 days)
            Map<String, Long> registrationTrend = new HashMap<>();
            for (int i = 0; i < 7; i++) {
                Date dayStart = getDateDaysAgo(i);
                Date dayEnd = getDateDaysAgo(i - 1);
                String dayKey = LocalDate.now().minusDays(i).toString();
                Long count = userRepository.countByAccountCreatedAtBetween(dayStart, dayEnd);
                registrationTrend.put(dayKey, count);
            }
            analytics.setUserRegistrationTrend(registrationTrend);

            // Daily active users (last 7 days)
            Map<String, Long> dailyActiveUsers = new HashMap<>();
            for (int i = 0; i < 7; i++) {
                Date dayStart = getDateDaysAgo(i);
                Date dayEnd = getDateDaysAgo(i - 1);
                String dayKey = LocalDate.now().minusDays(i).toString();
                Long count = userRepository.countByLastActiveAtBetween(dayStart, dayEnd);
                dailyActiveUsers.put(dayKey, count);
            }
            analytics.setDailyActiveUsers(dailyActiveUsers);

        } catch (Exception e) {
            // Log the error and set default values
            System.err.println("Error calculating analytics: " + e.getMessage());
            e.printStackTrace();

            // Set default values to prevent null pointer exceptions
            analytics.setTotalUsers(0L);
            analytics.setActiveUsers(0L);
            analytics.setNewUsersToday(0L);
            analytics.setNewUsersThisWeek(0L);
            analytics.setPremiumUsers(0L);
            analytics.setVerifiedUsers(0L);
            analytics.setTotalViews(0L);
            analytics.setTodayViews(0L);
            analytics.setThisWeekViews(0L);
            analytics.setUsersByRole(new HashMap<>());
            analytics.setUserRegistrationTrend(new HashMap<>());
            analytics.setDailyActiveUsers(new HashMap<>());
        }

        return analytics;
    }

    public void trackPageView(String userId, String page, String ipAddress, String userAgent, String sessionId) {
        PageView pageView = new PageView();
        pageView.setUserId(userId);
        pageView.setPage(page);
        pageView.setIpAddress(ipAddress);
        pageView.setUserAgent(userAgent);
        pageView.setSessionId(sessionId);
        pageView.setTimestamp(new Date());

        pageViewRepository.save(pageView);
    }

    private Date getDateDaysAgo(int days) {
        LocalDate date = LocalDate.now().minusDays(days);
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date getTodayStart() {
        LocalDate today = LocalDate.now();
        return Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
