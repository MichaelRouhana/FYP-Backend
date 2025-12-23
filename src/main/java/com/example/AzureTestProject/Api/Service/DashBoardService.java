package com.example.AzureTestProject.Api.Service;

import com.example.AzureTestProject.Api.Entity.Bet;
import com.example.AzureTestProject.Api.Entity.BetStatus;
import com.example.AzureTestProject.Api.Entity.User;
import com.example.AzureTestProject.Api.Model.ChartPoint;
import com.example.AzureTestProject.Api.Model.View.UserViewDTO;
import com.example.AzureTestProject.Api.Repository.BetRepository;
import com.example.AzureTestProject.Api.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
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
  //  private final LogRepository logRepository;


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


    //TODO
    public Object getLogs() {
        throw new NotImplementedException();
    }


    public List<UserViewDTO> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(u -> modelMapper.map(u, UserViewDTO.class))
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
