package com.example.AzureTestProject.Api.Service;


import com.example.AzureTestProject.Api.Entity.Organization;
import com.example.AzureTestProject.Api.Entity.WareHouse;
import com.example.AzureTestProject.Api.Exception.ApiRequestException;
import com.example.AzureTestProject.Api.Mapper.WareHouseMapper;
import com.example.AzureTestProject.Api.Model.Filter.WareHouseFilterDTO;
import com.example.AzureTestProject.Api.Model.Patch.WareHousePatchDTO;
import com.example.AzureTestProject.Api.Model.Request.WareHouseRequestDTO;
import com.example.AzureTestProject.Api.Model.Response.WareHouseResponseDTO;
import com.example.AzureTestProject.Api.Repository.OrganizationRepository;
import com.example.AzureTestProject.Api.Repository.WareHouseRepository;
import com.example.AzureTestProject.Api.Specification.GenericSpecification;
import com.example.AzureTestProject.Api.Util.PagedResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WareHouseService {


    private final WareHouseRepository wareHouseRepository;
    private final OrganizationRepository organizationRepository;
    private final ModelMapper modelMapper;
    private final WareHouseMapper wareHouseMapper;

    public PagedResponse<WareHouseResponseDTO> getAll(String organizationUUID, Pageable pageable, WareHouseFilterDTO filter) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        Specification<WareHouse> specification = Specification.where(
                GenericSpecification.<WareHouse>filterByFields(filter)
        ).and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("organization").get("id"), organization.getId())
        );

        Page<WareHouse> purchaseOrderPage = wareHouseRepository.findAll(specification, pageable);

        Page<WareHouseResponseDTO> accountResponsePage = purchaseOrderPage.map(account -> modelMapper.map(account, WareHouseResponseDTO.class));

        return PagedResponse.fromPage(accountResponsePage);
    }

    public void delete(String organizationUUID, String wareHouseUUID) {
        WareHouse wareHouse = wareHouseRepository.findByOrganization_UuidAndUuid(organizationUUID, wareHouseUUID)
                .orElseThrow(() -> new EntityNotFoundException("ware house not found"));

        if (wareHouse.getDefaultWareHouse())
            throw ApiRequestException.badRequest("cannot delete default warehouse");
        try {
            wareHouseRepository.delete(wareHouse);
        } catch (DataIntegrityViolationException e) {
            throw ApiRequestException.badRequest("Cannot delete wareHouse: it is referenced by other entities.");
        }
    }

    public WareHouseResponseDTO create(String organizationUUID, WareHouseRequestDTO wareHouseRequestDTO) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        WareHouse wareHouse = modelMapper.map(wareHouseRequestDTO, WareHouse.class);
        wareHouse.setOrganization(organization);
        wareHouse.setDefaultWareHouse(false);

        WareHouse savedWareHouse = wareHouseRepository.save(wareHouse);
        return modelMapper.map(savedWareHouse, WareHouseResponseDTO.class);
    }

    public WareHouseResponseDTO get(String organizationUUID, String wareHouseUUID) {
        WareHouse wareHouse = wareHouseRepository.findByOrganization_UuidAndUuid(organizationUUID, wareHouseUUID)
                .orElseThrow(() -> new EntityNotFoundException("ware house not found"));

        return modelMapper.map(wareHouse, WareHouseResponseDTO.class);
    }

    public void patch(String organizationUUID, String wareHouseUUID, WareHousePatchDTO wareHousePatchDTO) {
        WareHouse wareHouse = wareHouseRepository.findByOrganization_UuidAndUuid(organizationUUID, wareHouseUUID)
                .orElseThrow(() -> new EntityNotFoundException("warehouse not found"));

        wareHouseMapper.updateWareHouseFromPatchDTO(wareHousePatchDTO, wareHouse);

        wareHouseRepository.save(wareHouse);
    }
}
