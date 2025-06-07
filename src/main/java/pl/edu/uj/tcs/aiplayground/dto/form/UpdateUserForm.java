package pl.edu.uj.tcs.aiplayground.dto.form;

import java.time.LocalDate;

public record UpdateUserForm(
        String firstName,
        String lastName,
        String password,
        String country,
        LocalDate birthDate
) {
    public UpdateUserForm withHashedPassword(String hashedPassword) {
        return new UpdateUserForm(
                firstName,
                lastName,
                hashedPassword,
                country,
                birthDate
        );
    }
}