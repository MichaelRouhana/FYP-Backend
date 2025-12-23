package com.example.FYP.Api.Mapper;


import com.example.FYP.Api.Entity.WareHouse;
import com.example.FYP.Api.Model.Patch.WareHousePatchDTO;
import com.example.FYP.Api.Model.Request.WareHouseRequestDTO;
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
