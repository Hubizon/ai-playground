package pl.edu.uj.tcs.aiplayground.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UserDto(
        UUID userId,
        String username,
        String firstName,
        String lastName,
        String email,
        String countryName,
        LocalDate birthDate
) {
}
