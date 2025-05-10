package pl.edu.uj.tcs.aiplayground.form;

import java.time.LocalDate;

public record RegisterForm(
        String username,
        String firstName,
        String lastName,
        String email,
        String password,
        String country,
        LocalDate birthDate
) {
    public RegisterForm withHashedPassword(String hashedPassword) {
        return new RegisterForm(
                username,
                firstName,
                lastName,
                email,
                hashedPassword,
                country,
                birthDate
        );
    }
}
