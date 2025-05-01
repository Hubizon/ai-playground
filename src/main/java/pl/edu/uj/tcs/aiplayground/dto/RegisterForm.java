package pl.edu.uj.tcs.aiplayground.dto;

import java.time.LocalDate;

public record RegisterForm(
        String username,
        String firstName,
        String lastName,
        String email,
        String password,
        String country,
        LocalDate birthDate
) { }
