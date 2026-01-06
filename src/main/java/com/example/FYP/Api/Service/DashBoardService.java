package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.AuditLog;
import com.example.FYP.Api.Entity.Bet;
import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Model.ChartPoint;
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
        List<User> users = userRepository.findAll();
        return usersPerDate(users);
    }

    //TODO
    public List<ChartPoint> totalActiveUsers() {
        List<User> users = userRepository.findAll().stream()
                //.filter(User::getActive)
                .collect(Collectors.toList());
        return usersPerDate(users);
    }

    public List<ChartPoint> getTotalBets() {
        List<Bet> bets = betRepository.findAll();
        return betsPerDate(bets);
    }

    public List<ChartPoint> getWonBets() {
        List<Bet> bets = betRepository.findByStatus(BetStatus.WON);
        return betsPerDate(bets);
    }

    public List<ChartPoint> getLostBets() {
        List<Bet> bets = betRepository.findByStatus(BetStatus.LOST);
        return betsPerDate(bets);
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
                .map(u -> modelMapper.map(u, UserViewDTO.class))
                .toList();
    }

    public List<UserViewDTO> getTopPointers() {
        return userRepository.findTop10ByOrderByPointsDesc()
                .stream()
                .map(u -> modelMapper.map(u, UserViewDTO.class))
                .toList();
    }

    private List<ChartPoint> usersPerDate(List<User> users) {
        Map<String, Long> grouped = users.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedDate().toLocalDate().toString(),
                        Collectors.counting()
                ));

        return grouped.entrySet().stream()
                .map(e -> new ChartPoint(e.getKey(), e.getValue()))
                .sorted((a, b) -> a.getX().compareTo(b.getX()))
                .collect(Collectors.toList());
    }

    private List<ChartPoint> betsPerDate(List<Bet> bets) {
        Map<String, Long> grouped = bets.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getCreatedDate().toLocalDate().toString(),
                        Collectors.counting()
                ));

        return grouped.entrySet().stream()
                .map(e -> new ChartPoint(e.getKey(), e.getValue()))
                .sorted((a, b) -> a.getX().compareTo(b.getX()))
                .collect(Collectors.toList());
    }
}
