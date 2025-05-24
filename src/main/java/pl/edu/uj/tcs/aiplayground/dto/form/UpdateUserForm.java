package pl.edu.uj.tcs.aiplayground.dto.form;

import java.time.LocalDate;

public record UpdateUserForm(
        String email,
        String password,
        String country,
        LocalDate birthDate
) {
    public UpdateUserForm withHashedPassword(String hashedPassword) {
        return new UpdateUserForm(
                email,
                hashedPassword,
                country,
                birthDate
        );
    }
}