package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.AuditLog;
import com.example.FYP.Api.Entity.Bet;
import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Model.ChartPoint;
import com.example.FYP.Api.Model.View.DashboardStatsDTO;
import com.example.FYP.Api.Model.View.LogViewDTO;
import com.example.FYP.Api.Model.View.UserViewDTO;
import com.example.FYP.Api.Repository.AuditLogRepository;
import com.example.FYP.Api.Repository.BetRepository;
import com.example.FYP.Api.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashBoardService {

    private final UserRepository userRepository;
    private final BetRepository betRepository;
    private final ModelMapper modelMapper;
    private final AuditLogRepository auditLogRepository;


    public List<ChartPoint> totalUsers() {
        // Get last 7 days for the chart
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        
        // Fetch users created in the last 7 days (optimized query)
        List<User> recentUsers = userRepository.findByCreatedDateAfter(startDate);
        
        // Get total count of users created BEFORE the start date (for cumulative calculation)
        long usersBeforeStart = userRepository.countByCreatedDateBefore(startDate);
        
        log.info("📊 User Stats - Users before start date: {}, Recent users: {}", usersBeforeStart, recentUsers.size());
        
        // Group by date (daily new users) - handle timezone properly
        Map<LocalDate, Long> dailyNewUsers = recentUsers.stream()
                .filter(u -> u.getCreatedDate() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.counting()
                ));
        
        log.info("📊 User Stats Raw Data (Daily New): {}", dailyNewUsers);
        
        // Fill in missing days and calculate cumulative totals
        return buildCumulativeChart(dailyNewUsers, usersBeforeStart, 7);
    }

    public List<ChartPoint> totalActiveUsers() {
        // Get last 7 days for the chart
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        
        // For now, treat all users as "active" (TODO: implement actual active filter)
        // Use optimized query instead of fetching all users
        List<User> recentUsers = userRepository.findByCreatedDateAfter(startDate);
        // TODO: Add active filter when available: .filter(User::getActive)
        
        // Get total count of active users created BEFORE the start date
        long activeUsersBeforeStart = userRepository.countByCreatedDateBefore(startDate);
        // TODO: Add active filter when available
        
        log.info("📊 Active User Stats - Users before start date: {}, Recent users: {}", activeUsersBeforeStart, recentUsers.size());
        
        // Group by date (daily new active users) - handle timezone properly
        Map<LocalDate, Long> dailyNewActiveUsers = recentUsers.stream()
                .filter(u -> u.getCreatedDate() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.counting()
                ));
        
        log.info("📊 Active User Stats Raw Data (Daily New): {}", dailyNewActiveUsers);
        
        // Fill in missing days and calculate cumulative totals
        return buildCumulativeChart(dailyNewActiveUsers, activeUsersBeforeStart, 7);
    }

    public List<ChartPoint> getTotalBets() {
        List<Bet> bets = betRepository.findAll();
        return betsPerDateDistinctTickets(bets);
    }

    public List<ChartPoint> getWonBets() {
        List<Bet> bets = betRepository.findByStatus(BetStatus.WON);
        return betsPerDateDistinctTickets(bets);
    }

    public List<ChartPoint> getLostBets() {
        List<Bet> bets = betRepository.findByStatus(BetStatus.LOST);
        return betsPerDateDistinctTickets(bets);
    }


    public List<LogViewDTO> getLogs() {
        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "performedAt")).stream()
                .map(log -> {
                    LogViewDTO dto = new LogViewDTO();
                    dto.setId(log.getId());
                    dto.setAction(log.getAction());
                    
                    // Combine oldData and newData as details, or use entityName + entityId
                    String details = String.format("%s (ID: %s)", log.getEntityName(), log.getEntityId());
                    if (log.getOldData() != null || log.getNewData() != null) {
                        details += " - " + (log.getNewData() != null ? log.getNewData() : log.getOldData());
                    }
                    dto.setDetails(details);
                    
                    dto.setTimestamp(log.getPerformedAt());
                    
                    // performedBy is already a String (username), not a User object
                    dto.setUsername(log.getPerformedBy() != null ? log.getPerformedBy() : "System");
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public List<UserViewDTO> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(u -> {
                    UserViewDTO dto = modelMapper.map(u, UserViewDTO.class);
                    // Explicitly map points to totalPoints since field names differ
                    if (dto.getTotalPoints() == null && u.getPoints() != null) {
                        dto.setTotalPoints(u.getPoints());
                    }
                    return dto;
                })
                .toList();
    }

    public List<UserViewDTO> getTopBetters() {
        return userRepository.findTop10UsersByBetCount()
                .stream()
                .map(user -> {
                    UserViewDTO dto = modelMapper.map(user, UserViewDTO.class);
                    
                    // Count distinct tickets (not legs) for this user
                    long distinctTickets = betRepository.countDistinctTicketsByUserId(user.getId());
                    long nullTicketBets = user.getBets() != null ? user.getBets().stream()
                            .filter(bet -> bet.getTicketId() == null || bet.getTicketId().isEmpty())
                            .count() : 0L;
                    dto.setTotalBets(distinctTickets + nullTicketBets);
                    
                    // Ensure points are set (map points -> totalPoints)
                    dto.setTotalPoints(user.getPoints() != null ? user.getPoints() : 0L);
                    
                    return dto;
                })
                .toList();
    }

    public List<UserViewDTO> getTopPointers() {
        return userRepository.findTop10ByOrderByPointsDesc()
                .stream()
                .map(user -> {
                    UserViewDTO dto = modelMapper.map(user, UserViewDTO.class);
                    
                    // Ensure points are set (map points -> totalPoints)
                    dto.setTotalPoints(user.getPoints() != null ? user.getPoints() : 0L);
                    
                    // Count distinct tickets (not legs) for this user
                    long distinctTickets = betRepository.countDistinctTicketsByUserId(user.getId());
                    long nullTicketBets = user.getBets() != null ? user.getBets().stream()
                            .filter(bet -> bet.getTicketId() == null || bet.getTicketId().isEmpty())
                            .count() : 0L;
                    dto.setTotalBets(distinctTickets + nullTicketBets);
                    
                    return dto;
                })
                .toList();
    }

    public DashboardStatsDTO getDashboardStats(String timeRange) {
        // 1. Determine the Date Threshold
        java.time.LocalDateTime threshold;

        if ("24h".equalsIgnoreCase(timeRange)) {
            threshold = java.time.LocalDateTime.now().minusHours(24);
        } else if ("7d".equalsIgnoreCase(timeRange)) {
            threshold = java.time.LocalDateTime.now().minusDays(7);
        } else {
            // Default "All Time" - count distinct tickets (not legs)
            // Count distinct ticketIds + bets with NULL ticketId
            long distinctTickets = betRepository.countDistinctTickets();
            long nullTicketBets = betRepository.findAll().stream()
                    .filter(bet -> bet.getTicketId() == null || bet.getTicketId().isEmpty())
                    .count();
            long totalBets = distinctTickets + nullTicketBets;
            
            long distinctWonTickets = betRepository.countDistinctTicketsByStatus(BetStatus.WON);
            long nullTicketWonBets = betRepository.findByStatus(BetStatus.WON).stream()
                    .filter(bet -> bet.getTicketId() == null || bet.getTicketId().isEmpty())
                    .count();
            long wonBets = distinctWonTickets + nullTicketWonBets;
            
            long distinctLostTickets = betRepository.countDistinctTicketsByStatus(BetStatus.LOST);
            long nullTicketLostBets = betRepository.findByStatus(BetStatus.LOST).stream()
                    .filter(bet -> bet.getTicketId() == null || bet.getTicketId().isEmpty())
                    .count();
            long lostBets = distinctLostTickets + nullTicketLostBets;
            
            return DashboardStatsDTO.builder()
                    .totalBets(totalBets)
                    .wonBets(wonBets)
                    .lostBets(lostBets)
                    .build();
        }

        // 2. Return filtered counts - count distinct tickets (not legs)
        long distinctTickets = betRepository.countDistinctTicketsByCreatedDateAfter(threshold);
        long nullTicketBets = betRepository.findAll().stream()
                .filter(bet -> bet.getCreatedDate() != null && 
                        bet.getCreatedDate().isAfter(threshold) &&
                        (bet.getTicketId() == null || bet.getTicketId().isEmpty()))
                .count();
        long totalBets = distinctTickets + nullTicketBets;
        
        long distinctWonTickets = betRepository.countDistinctTicketsByStatusAndCreatedDateAfter(BetStatus.WON, threshold);
        long nullTicketWonBets = betRepository.findByStatus(BetStatus.WON).stream()
                .filter(bet -> bet.getCreatedDate() != null && 
                        bet.getCreatedDate().isAfter(threshold) &&
                        (bet.getTicketId() == null || bet.getTicketId().isEmpty()))
                .count();
        long wonBets = distinctWonTickets + nullTicketWonBets;
        
        long distinctLostTickets = betRepository.countDistinctTicketsByStatusAndCreatedDateAfter(BetStatus.LOST, threshold);
        long nullTicketLostBets = betRepository.findByStatus(BetStatus.LOST).stream()
                .filter(bet -> bet.getCreatedDate() != null && 
                        bet.getCreatedDate().isAfter(threshold) &&
                        (bet.getTicketId() == null || bet.getTicketId().isEmpty()))
                .count();
        long lostBets = distinctLostTickets + nullTicketLostBets;
        
        return DashboardStatsDTO.builder()
                .totalBets(totalBets)
                .wonBets(wonBets)
                .lostBets(lostBets)
                .build();
    }

    /**
     * Build cumulative chart with filled-in missing days
     * @param dailyData Map of date -> daily count
     * @param initialCount Users created before the date range
     * @param days Number of days to show (default 7)
     * @return List of ChartPoint with cumulative totals and all days filled
     */
    private List<ChartPoint> buildCumulativeChart(Map<LocalDate, Long> dailyData, long initialCount, int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);
        
        List<ChartPoint> chartPoints = new ArrayList<>();
        long runningTotal = initialCount;
        
        // Iterate through each day from startDate to today
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            long dailyCount = dailyData.getOrDefault(currentDate, 0L);
            runningTotal += dailyCount;
            
            // Format date as string (YYYY-MM-DD)
            String dateStr = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            chartPoints.add(new ChartPoint(dateStr, runningTotal));
        }
        
        log.info("📊 User Stats Final Chart Points: {}", chartPoints);
        return chartPoints;
    }
    
    /**
     * Legacy method - kept for backward compatibility
     * @deprecated Use buildCumulativeChart instead
     */
    @Deprecated
    private List<ChartPoint> usersPerDate(List<User> users) {
        Map<String, Long> grouped = users.stream()
                .filter(u -> u.getCreatedDate() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                        Collectors.counting()
                ));

        return grouped.entrySet().stream()
                .map(e -> new ChartPoint(e.getKey(), e.getValue()))
                .sorted((a, b) -> a.getX().compareTo(b.getX()))
                .collect(Collectors.toList());
    }

    private List<ChartPoint> betsPerDate(List<Bet> bets) {
        // Get last 7 days for the chart
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        
        // Filter bets from last 7 days
        List<Bet> recentBets = bets.stream()
                .filter(b -> b.getCreatedDate() != null && b.getCreatedDate().isAfter(startDate))
                .collect(Collectors.toList());
        
        // Group by date (daily new bets)
        Map<LocalDate, Long> dailyBets = recentBets.stream()
                .filter(b -> b.getCreatedDate() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.counting()
                ));
        
        log.info("📊 Bet Stats Raw Data (Daily): {}", dailyBets);
        
        // Fill in missing days (for bets, we show daily counts, not cumulative)
        return buildDailyChart(dailyBets, 7);
    }

    /**
     * Count distinct tickets (not legs) per date
     */
    private List<ChartPoint> betsPerDateDistinctTickets(List<Bet> bets) {
        // Get last 7 days for the chart
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        
        // Filter bets from last 7 days
        List<Bet> recentBets = bets.stream()
                .filter(b -> b.getCreatedDate() != null && b.getCreatedDate().isAfter(startDate))
                .collect(Collectors.toList());
        
        // Group by date and count distinct tickets (not legs)
        // For each date, count distinct ticketIds + bets with NULL ticketId
        Map<LocalDate, Long> dailyTickets = recentBets.stream()
                .filter(b -> b.getCreatedDate() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                betList -> {
                                    // Count distinct ticketIds
                                    long distinctTickets = betList.stream()
                                            .filter(b -> b.getTicketId() != null && !b.getTicketId().isEmpty())
                                            .map(Bet::getTicketId)
                                            .distinct()
                                            .count();
                                    // Count bets with NULL ticketId (each is a separate ticket)
                                    long nullTicketBets = betList.stream()
                                            .filter(b -> b.getTicketId() == null || b.getTicketId().isEmpty())
                                            .count();
                                    return distinctTickets + nullTicketBets;
                                }
                        )
                ));
        
        log.info("📊 Bet Stats Raw Data (Daily Distinct Tickets): {}", dailyTickets);
        
        // Fill in missing days (for bets, we show daily counts, not cumulative)
        return buildDailyChart(dailyTickets, 7);
    }
    
    /**
     * Build daily chart with filled-in missing days (non-cumulative)
     * @param dailyData Map of date -> daily count
     * @param days Number of days to show (default 7)
     * @return List of ChartPoint with daily counts and all days filled
     */
    private List<ChartPoint> buildDailyChart(Map<LocalDate, Long> dailyData, int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);
        
        List<ChartPoint> chartPoints = new ArrayList<>();
        
        // Iterate through each day from startDate to today
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            long dailyCount = dailyData.getOrDefault(currentDate, 0L);
            
            // Format date as string (YYYY-MM-DD)
            String dateStr = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            chartPoints.add(new ChartPoint(dateStr, dailyCount));
        }
        
        return chartPoints;
    }
}
