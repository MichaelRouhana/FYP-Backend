package com.example.AzureTestProject.Api.Mapper;


import com.example.AzureTestProject.Api.Entity.WareHouse;
import com.example.AzureTestProject.Api.Model.Patch.WareHousePatchDTO;
import com.example.AzureTestProject.Api.Model.Request.WareHouseRequestDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface WareHouseMapper {

    WareHouse toEntity(WareHouseRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateWareHouseFromPatchDTO(WareHousePatchDTO dto, @MappingTarget WareHouse entity);
}
