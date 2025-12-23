package com.example.AzureTestProject.Api.Util;

import com.example.AzureTestProject.Api.Service.SequenceGeneratorService;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class CodeGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    @Setter
    private static SequenceGeneratorService sequenceGeneratorService;

    private CodeGenerator() {
    }

    public static String generateCode(String prefix) {
        if (sequenceGeneratorService == null) {
            throw new IllegalStateException("SequenceGeneratorService not initialized");
        }
        long seq = sequenceGeneratorService.getNextSequence();
        String today = LocalDate.now().format(DATE_FMT);
        return String.format("%s-%s-%06d", prefix.toUpperCase(), today, seq);
    }
}
