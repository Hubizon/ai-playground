package pl.edu.uj.tcs.aiplayground.service.repository;

import pl.edu.uj.tcs.aiplayground.dto.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.dto.form.UpdateUserForm;
import pl.edu.uj.tcs.jooq.tables.records.UsersRecord;

import java.util.List;
import java.util.UUID;

public interface IUserRepository {
    boolean existUsername(String username);

    boolean existEmail(String email);

    UsersRecord findByUsername(String username);

    void insertUser(RegisterForm user);

    List<String> getCountries();

    Integer getCountryIdByName(String countryName);

    String getCountryNameById(Integer countryId);

    void updateUser(UUID userId, UpdateUserForm updateUserForm);

    int userTokenCount(UUID userId);
}
