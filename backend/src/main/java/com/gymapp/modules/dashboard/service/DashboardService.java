package com.gymapp.modules.dashboard.service;

import com.gymapp.modules.booking.repository.BookingRepository;
import com.gymapp.modules.dashboard.dto.DashboardResponse;
import com.gymapp.modules.membership.enums.MembershipStatus;
import com.gymapp.modules.membership.repository.MembershipRepository;
import com.gymapp.modules.payment.repository.PaymentRepository;
import com.gymapp.modules.user.repository.CheckinLogRepository;
import com.gymapp.modules.user.repository.CheckinRepository;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final CheckinRepository checkinRepository;
    private final CheckinLogRepository checkinLogRepository;

    public DashboardResponse getDashboard() {
        try {
            return buildDashboard();
        } catch (Exception ex) {
            log.error("Unexpected error while building admin dashboard. Returning safe fallback response.", ex);
            return fallbackDashboard(LocalDate.now(APP_ZONE));
        }
    }

    private DashboardResponse buildDashboard() {
        LocalDate today = LocalDate.now(APP_ZONE);
        LocalDate firstChartDate = today.minusDays(6);

        OffsetDateTime startOfToday = startOfDay(today);
        OffsetDateTime startOfTomorrow = startOfDay(today.plusDays(1));
        OffsetDateTime startOfMonth = startOfDay(today.withDayOfMonth(1));
        OffsetDateTime startOfNextMonth = startOfDay(today.plusMonths(1).withDayOfMonth(1));
        OffsetDateTime chartStart = startOfDay(firstChartDate);

        log.info(
                "Building admin dashboard: today={}, startOfToday={}, startOfTomorrow={}, startOfMonth={}, startOfNextMonth={}, chartStart={}",
                today, startOfToday, startOfTomorrow, startOfMonth, startOfNextMonth, chartStart
        );

        long totalUsers = safeLong("totalUsers", userRepository::count);
        long activeMembers = safeLong(
                "activeMembers",
                () -> membershipRepository.countDistinctActiveMembers(MembershipStatus.ACTIVE, today)
        );
        BigDecimal monthlyRevenue = safeBigDecimal(
                "monthlyRevenue",
                () -> paymentRepository.sumSuccessfulRevenueBetween(startOfMonth, startOfNextMonth)
        );
        long todayBookings = safeLong(
                "todayBookings",
                () -> bookingRepository.countByScheduledAtGreaterThanEqualAndScheduledAtLessThan(startOfToday, startOfTomorrow)
        );
        long legacyCheckinsToday = safeLong(
                "legacyCheckinsToday",
                () -> checkinRepository.countByCheckinDate(today)
        );
        long qrCheckinsToday = safeLong(
                "qrCheckinsToday",
                () -> checkinLogRepository.countByCheckinDate(today)
        );
        long todayCheckins = legacyCheckinsToday + qrCheckinsToday;

        Map<LocalDate, BigDecimal> revenueByDate =
                safeList("dailyRevenue", () -> paymentRepository.findDailyRevenueBetween(chartStart, startOfTomorrow))
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(item -> {
                            if (item.getRevenueDate() == null) {
                                log.warn("Ignoring daily revenue projection with null date: revenue={}", item.getRevenue());
                                return false;
                            }
                            return true;
                        })
                        .collect(Collectors.toMap(
                                PaymentRepository.DailyRevenueProjection::getRevenueDate,
                                item -> safeBigDecimal(item.getRevenue()),
                                BigDecimal::add
                        ));

        log.info("Revenue by date mapping: {}", revenueByDate);

        var revenueLast7Days = IntStream.rangeClosed(0, 6)
                .mapToObj(firstChartDate::plusDays)
                .map(date -> DashboardResponse.RevenuePoint.builder()
                        .date(DATE_FORMATTER.format(date))
                        .revenue(revenueByDate.getOrDefault(date, BigDecimal.ZERO))
                        .build())
                .toList();

        var topPts = safeList("topPtRevenue", paymentRepository::findTopPtRevenue)
                .stream()
                .filter(Objects::nonNull)
                .map(item -> DashboardResponse.TopPt.builder()
                        .name(Optional.ofNullable(item.getName()).orElse(""))
                        .revenue(safeBigDecimal(item.getRevenue()))
                        .build())
                .toList();

        log.info("Last 7-day revenue: {}", revenueLast7Days);
        log.info("Top PTs: {}", topPts);
        log.info("Monthly revenue: {}", monthlyRevenue);
        log.info("Check-ins today: legacy={}, qr={}, total={}", legacyCheckinsToday, qrCheckinsToday, todayCheckins);
        log.info("Active members: {}", activeMembers);

        DashboardResponse response = DashboardResponse.builder()
                .totalUsers(totalUsers)
                .activeMembers(activeMembers)
                .monthlyRevenue(monthlyRevenue)
                .todayBookings(todayBookings)
                .todayCheckins(todayCheckins)
                .revenueLast7Days(revenueLast7Days)
                .topPTs(topPts)
                .build();

        log.info("Returning admin dashboard response: {}", response);
        return response;
    }

    private DashboardResponse fallbackDashboard(LocalDate today) {
        LocalDate firstChartDate = today.minusDays(6);

        var revenueLast7Days = IntStream.rangeClosed(0, 6)
                .mapToObj(firstChartDate::plusDays)
                .map(date -> DashboardResponse.RevenuePoint.builder()
                        .date(DATE_FORMATTER.format(date))
                        .revenue(BigDecimal.ZERO)
                        .build())
                .toList();

        return DashboardResponse.builder()
                .totalUsers(0L)
                .activeMembers(0L)
                .monthlyRevenue(BigDecimal.ZERO)
                .todayBookings(0L)
                .todayCheckins(0L)
                .revenueLast7Days(revenueLast7Days)
                .topPTs(Collections.emptyList())
                .build();
    }

    private OffsetDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay(APP_ZONE).toOffsetDateTime();
    }

    private long safeLong(String metricName, LongSupplier supplier) {
        try {
            long value = supplier.getAsLong();
            log.info("{}: {}", metricName, value);
            return value;
        } catch (Exception ex) {
            log.error("Failed to load dashboard metric '{}'. Returning 0.", metricName, ex);
            return 0L;
        }
    }

    private BigDecimal safeBigDecimal(String metricName, Supplier<BigDecimal> supplier) {
        try {
            BigDecimal value = safeBigDecimal(supplier.get());
            log.info("{}: {}", metricName, value);
            return value;
        } catch (Exception ex) {
            log.error("Failed to load dashboard metric '{}'. Returning 0.", metricName, ex);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal safeBigDecimal(BigDecimal value) {
        return Optional.ofNullable(value).orElse(BigDecimal.ZERO);
    }

    private <T> List<T> safeList(String metricName, Supplier<List<T>> supplier) {
        try {
            List<T> values = Optional.ofNullable(supplier.get()).orElse(Collections.emptyList());
            if (values.isEmpty()) {
                log.info("{} returned an empty list.", metricName);
            } else {
                log.info("{} returned {} item(s).", metricName, values.size());
            }
            return values;
        } catch (Exception ex) {
            log.error("Failed to load dashboard list '{}'. Returning empty list.", metricName, ex);
            return Collections.emptyList();
        }
    }
}
