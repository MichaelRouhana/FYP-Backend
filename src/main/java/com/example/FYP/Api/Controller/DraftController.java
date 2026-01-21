package com.example.FYP.Api.Controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@Validated
@RequestMapping("/draft")
public class DraftController {


    @GetMapping("/testRole")
    @PreAuthorize("hasRole('ROLE_DEVELOPER')")
    public ResponseEntity<String> testRole() {
        throw new NotImplementedException("not implemented");
    }


    @PostMapping("/testRoleType")
    public ResponseEntity<String> testRoleType(@RequestParam() String organizationUUID, @RequestBody TestRole request) {
        throw new NotImplementedException("not implemented");
    }

    @PostMapping("/testValidation/{testPath}")
    public ResponseEntity<TestModel> create(@PathVariable @NotBlank String testPath, @RequestBody @Valid TestModel testModel) {
        return ResponseEntity.ok(testModel);
    }

    @PostMapping("/instant/{timestamp}")
    public ResponseEntity<Object> testInstant(@NotBlank Instant timestamp) {
        System.out.println(timestamp.toEpochMilli());
        return ResponseEntity.ok(timestamp);
    }

    @GetMapping("/testParam")
    public ResponseEntity<String> create(@RequestParam("testUUID") @NotBlank(message = "testUUID cannot be blank") String testUUID) {
        return ResponseEntity.ok(testUUID);
    }

    @PostMapping("/enum")
    public ResponseEntity<Object> enumt(@RequestBody TestModel testModel) {

        System.out.println(testModel.toString());
        return ResponseEntity.ok(testModel);
    }

    @GetMapping("/testCondVal")
    public ResponseEntity<Object> create(@Valid @RequestBody CondVal request) {
        return ResponseEntity.ok(request);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestRole {
        private String type;

    }

    @Data
    public static class CondVal {
        private boolean inspection;

        private String extraField = "";

        @AssertTrue(message = "validation error")
        public boolean valid() {
            if (inspection) {
                return extraField != null && !extraField.isEmpty();
            } else {
                return extraField == null && extraField.isEmpty();
            }
        }
    }


}
