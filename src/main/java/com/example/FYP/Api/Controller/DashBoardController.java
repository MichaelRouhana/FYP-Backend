package com.example.FYP.Api.Controller;


import com.example.FYP.Api.Loader.Annotation.Feature;
import com.example.FYP.Api.Service.DashBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@Validated
@Tag(name = "DashBoard Controller", description = "provides basic functionalities for dashboard's")
@PreAuthorize("hasAuthority('ADMIN')")
@Feature
public class  DashBoardController {

    @Autowired
    private DashBoardService dashboardService;

    //plot
    @Operation(summary = "retrieve dashboard's totalUsers",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @GetMapping("/totalUsers")
    public ResponseEntity<?> totalUsers() {
        return ResponseEntity.ok(dashboardService.totalUsers());
    }

    //plot
    @Operation(summary = "retrieve dashboard's totalActiveUsers",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @GetMapping("/totalActiveUsers")
    public ResponseEntity<?> totalActiveUsers() {
        return ResponseEntity.ok(dashboardService.totalActiveUsers());
    }

    //list
    @Operation(summary = "retrieve dashboard's users",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(dashboardService.getUsers());
    }


    //logs
    @Operation(summary = "retrieve dashboard's logs",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @GetMapping("/logs")
    public ResponseEntity<?> getLogs() {
        return ResponseEntity.ok(dashboardService.getLogs());
    }


    //plot
    @Operation(summary = "retrieve dashboard's totalBets",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @GetMapping("/totalBets")
    public ResponseEntity<?> getTotalBets() {
        return ResponseEntity.ok(dashboardService.getTotalBets());
    }


    //plot
    @Operation(summary = "retrieve dashboard's wonBets",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @GetMapping("/wonBets")
    public ResponseEntity<?> getWonBets() {
        return ResponseEntity.ok(dashboardService.getWonBets());
    }

    //plot
    @Operation(summary = "retrieve dashboard's lostBets",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @GetMapping("/lostBets")
    public ResponseEntity<?> getLostBets() {
        return ResponseEntity.ok(dashboardService.getLostBets());
    }

    //list
    @Operation(summary = "retrieve dashboard's topBetters",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @GetMapping("/topBetters")
    public ResponseEntity<?> getTopBetters() {
        return ResponseEntity.ok(dashboardService.getTopBetters());
    }

    //list
    @Operation(summary = "retrieve dashboard's topPointers",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @GetMapping("/topPointers")
    public ResponseEntity<?> getTopPointers() {
        return ResponseEntity.ok(dashboardService.getTopPointers());
    }






}
