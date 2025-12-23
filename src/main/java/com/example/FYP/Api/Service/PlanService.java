package com.example.FYP.Api.Service;

import com.example.FYP.Api.Model.View.PlanViewDTO;
import com.example.FYP.Api.Repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final ModelMapper modelMapper;


    public List<PlanViewDTO> getAll(){
        return planRepository.findAll()
                .stream()
                .map((e) -> modelMapper.map(e, PlanViewDTO.class))
                .collect(Collectors.toList());
    }
}
