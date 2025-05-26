package pl.edu.uj.tcs.aiplayground.dto.form;

import org.jooq.JSONB;

import java.util.UUID;

public record ModelForm(
        UUID userId,
        String name,
        JSONB jsonArchitecture
) {
}
