package pl.edu.uj.tcs.aiplayground.dto;

import org.jooq.JSONB;

import java.util.UUID;

public record ModelDto(
        UUID modelId,
        UUID userId,
        UUID modelVersionId,
        String modelName,
        Integer versionNumber,
        JSONB architecture
) {
}
