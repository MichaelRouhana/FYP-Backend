package com.example.AzureTestProject.Api.Util;

import com.example.AzureTestProject.Api.Service.SequenceGeneratorService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class CodeGeneratorInitializer {

    private final SequenceGeneratorService sequenceGeneratorService;

    public CodeGeneratorInitializer(SequenceGeneratorService sequenceGeneratorService) {
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @PostConstruct
    public void init() {
        CodeGenerator.setSequenceGeneratorService(sequenceGeneratorService);
    }
}
