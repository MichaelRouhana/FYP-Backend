package com.example.AzureTestProject.Api.Controller;

import com.example.AzureTestProject.Api.Interceptor.Annotation.RequiredRole;
import com.example.AzureTestProject.Api.Loader.Annotation.Feature;
import com.example.AzureTestProject.Api.Model.Constant.OrganizationRoles;
import com.example.AzureTestProject.Api.Model.Filter.WareHouseFilterDTO;
import com.example.AzureTestProject.Api.Model.Patch.WareHousePatchDTO;
import com.example.AzureTestProject.Api.Model.Request.WareHouseRequestDTO;
import com.example.AzureTestProject.Api.Model.Response.WareHouseResponseDTO;
import com.example.AzureTestProject.Api.Service.WareHouseService;
import com.example.AzureTestProject.Api.Util.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warehouses")
@Validated
@Tag(name = "WareHouse Controller", description = "provides basic functionalities for warehouse's")
@RequiredRole({OrganizationRoles.OWNER, OrganizationRoles.MEMBER})
@Feature
public class  WareHouseController {

    @Autowired
    private WareHouseService wareHouseService;

    @Operation(summary = "retrieve warehouse",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),

            },
            responses = {
                    @ApiResponse(description = "retrieved warehouse", responseCode = "200", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WareHouseResponseDTO.class))),
            })
    @GetMapping("/{wareHouseUUID}")
    public ResponseEntity<WareHouseResponseDTO> get(@RequestParam("organizationUUID") String organizationUUID,
                                                    @PathVariable String wareHouseUUID) {
        return ResponseEntity.ok(wareHouseService.get(organizationUUID, wareHouseUUID));
    }

    @Operation(summary = "create warehouse",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true)

            },
            responses = {
                    @ApiResponse(description = "WareHouse created", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = WareHouseResponseDTO.class))),
            })
    @PostMapping
    public ResponseEntity<WareHouseResponseDTO> create(@RequestParam("organizationUUID") @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                                       @RequestBody @Valid WareHouseRequestDTO wareHouseDTO) {
        return ResponseEntity.ok(wareHouseService.create(organizationUUID, wareHouseDTO));
    }


    @Operation(summary = "delete wareHouse",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),

            },
            responses = {
                    @ApiResponse(description = "wareHouse Deleted", responseCode = "200"),

            })
    @DeleteMapping("/{wareHouseUUID}")
    public ResponseEntity<Void> delete(@PathVariable @NotBlank(message = "organizationUUID cannot be blank") String wareHouseUUID,
                                       @RequestParam("organizationUUID") String organizationUUID) {

        wareHouseService.delete(organizationUUID, wareHouseUUID);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "retrieve wareHouse's",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true)

            },
            responses = {
                    @ApiResponse(description = "wareHouse's retrieved Successfully!", responseCode = "200", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WareHouseResponseDTO.class)))
            })
    @GetMapping
    public ResponseEntity<PagedResponse<WareHouseResponseDTO>> getAll(@RequestParam("organizationUUID") @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                                                      Pageable pageable,
                                                                      WareHouseFilterDTO filter) {
        return ResponseEntity.ok(wareHouseService.getAll(organizationUUID, pageable, filter));
    }


    @Operation(summary = "patch warehouse",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),

            },
            responses = {
                    @ApiResponse(description = "warehouse patched", responseCode = "200"),
            })
    @PatchMapping("/{wareHouseUUID}")
    public ResponseEntity<Void> patch(@PathVariable @NotBlank(message = "wareHouseUUID cannot be blank") String wareHouseUUID,
                                      @RequestParam("organizationUUID") @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                      @RequestBody @Valid WareHousePatchDTO wareHousePatchDTO) {
        wareHouseService.patch(organizationUUID, wareHouseUUID, wareHousePatchDTO);
        return ResponseEntity.ok().build();
    }
}
