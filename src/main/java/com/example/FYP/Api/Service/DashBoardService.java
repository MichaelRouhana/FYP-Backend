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
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        
        List<User> recentUsers = userRepository.findByCreatedDateAfter(startDate);
        
        long usersBeforeStart = userRepository.countByCreatedDateBefore(startDate);
        
        log.info("📊 User Stats - Users before start date: {}, Recent users: {}", usersBeforeStart, recentUsers.size());
        
        Map<LocalDate, Long> dailyNewUsers = recentUsers.stream()
                .filter(u -> u.getCreatedDate() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.counting()
                ));
        
        log.info("📊 User Stats Raw Data (Daily New): {}", dailyNewUsers);
        
        return buildCumulativeChart(dailyNewUsers, usersBeforeStart, 7);
    }

    public List<ChartPoint> totalActiveUsers() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        
        LocalDateTime activityThreshold = LocalDateTime.now().minusDays(30);
        
        List<User> recentActiveUsers = userRepository.findActiveUsersByCreatedDateAfter(startDate, activityThreshold);
        
        long activeUsersBeforeStart = userRepository.countActiveUsersByCreatedDateBefore(startDate, activityThreshold);
        
        log.info("📊 Active User Stats - Active users before start date: {}, Recent active users: {}", 
                activeUsersBeforeStart, recentActiveUsers.size());
        
        Map<LocalDate, Long> dailyNewActiveUsers = recentActiveUsers.stream()
                .filter(u -> u.getCreatedDate() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.counting()
                ));
        
        log.info("📊 Active User Stats Raw Data (Daily New): {}", dailyNewActiveUsers);
        
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
                    
                    String details = String.format("%s (ID: %s)", log.getEntityName(), log.getEntityId());
                    if (log.getOldData() != null || log.getNewData() != null) {
                        details += " - " + (log.getNewData() != null ? log.getNewData() : log.getOldData());
                    }
                    dto.setDetails(details);
                    
                    dto.setTimestamp(log.getPerformedAt());
                    
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
                    
                    long distinctTickets = betRepository.countDistinctTicketsByUserId(user.getId());
                    long nullTicketBets = user.getBets() != null ? user.getBets().stream()
                            .filter(bet -> bet.getTicketId() == null || bet.getTicketId().isEmpty())
                            .count() : 0L;
                    dto.setTotalBets(distinctTickets + nullTicketBets);
                    
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
                    
                    dto.setTotalPoints(user.getPoints() != null ? user.getPoints() : 0L);
                    
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
        java.time.LocalDateTime threshold;

        if ("24h".equalsIgnoreCase(timeRange)) {
            threshold = java.time.LocalDateTime.now().minusHours(24);
        } else if ("7d".equalsIgnoreCase(timeRange)) {
            threshold = java.time.LocalDateTime.now().minusDays(7);
        } else {
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
        
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            long dailyCount = dailyData.getOrDefault(currentDate, 0L);
            runningTotal += dailyCount;
            
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
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        
        List<Bet> recentBets = bets.stream()
                .filter(b -> b.getCreatedDate() != null && b.getCreatedDate().isAfter(startDate))
                .collect(Collectors.toList());
        
        Map<LocalDate, Long> dailyBets = recentBets.stream()
                .filter(b -> b.getCreatedDate() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.counting()
                ));
        
        log.info("📊 Bet Stats Raw Data (Daily): {}", dailyBets);
        
        return buildDailyChart(dailyBets, 7);
    }

    /**
     * Count distinct tickets (not legs) per date
     */
    private List<ChartPoint> betsPerDateDistinctTickets(List<Bet> bets) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        
        List<Bet> recentBets = bets.stream()
                .filter(b -> b.getCreatedDate() != null && b.getCreatedDate().isAfter(startDate))
                .collect(Collectors.toList());
        
        Map<LocalDate, Long> dailyTickets = recentBets.stream()
                .filter(b -> b.getCreatedDate() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                betList -> {
                                    long distinctTickets = betList.stream()
                                            .filter(b -> b.getTicketId() != null && !b.getTicketId().isEmpty())
                                            .map(Bet::getTicketId)
                                            .distinct()
                                            .count();
                                    long nullTicketBets = betList.stream()
                                            .filter(b -> b.getTicketId() == null || b.getTicketId().isEmpty())
                                            .count();
                                    return distinctTickets + nullTicketBets;
                                }
                        )
                ));
        
        log.info("📊 Bet Stats Raw Data (Daily Distinct Tickets): {}", dailyTickets);
        
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
        
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            long dailyCount = dailyData.getOrDefault(currentDate, 0L);
            
            String dateStr = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            chartPoints.add(new ChartPoint(dateStr, dailyCount));
        }
        
        return chartPoints;
    }
}
