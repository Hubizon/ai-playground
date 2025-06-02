package pl.edu.uj.tcs.aiplayground.service.repository;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.dto.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.dto.form.UpdateUserForm;
import pl.edu.uj.tcs.jooq.tables.records.UsersRecord;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public class UserRepository implements IUserRepository {
    private final DSLContext dsl;

    public UserRepository(DSLContext dslContext) {
        this.dsl = dslContext;
    }

    @Override
    public boolean existUsername(String username) {
        return dsl.fetchOne(
                "SELECT EXISTS (SELECT * FROM users WHERE username = ?);",
                username
        ).into(Boolean.class);
    }

    @Override
    public boolean existEmail(String email) {
        return dsl.fetchOne(
                "SELECT EXISTS (SELECT * FROM users WHERE email = ?);",
                email
        ).into(Boolean.class);
    }

    @Override
    public UsersRecord findByUsername(String username) {
        return dsl.fetchOne("""
                        SELECT *
                            FROM users
                            WHERE username = ?;
                        """,
                username
        ).into(UsersRecord.class);
    }

    @Override
    public void insertUser(RegisterForm registerForm) {
        dsl.query("""
                        INSERT INTO users(username, first_name, last_name, email, password_hash, country_id, birth_date, created_at)
                            VALUES (?,
                                    ?,
                                    ?,
                                    ?,
                                    ?,
                                    (SELECT id FROM countries WHERE name = ?),
                                    ?,
                                    now())
                        """,
                registerForm.username(),
                registerForm.firstName(),
                registerForm.lastName(),
                registerForm.email(),
                registerForm.password(),
                registerForm.country(),
                registerForm.birthDate()
        ).execute();
    }

    @Override
    public List<String> getCountries() {
        return dsl.fetch("""
                SELECT name
                    FROM countries;
                """
        ).into(String.class);
    }

    @Override
    public Integer getCountryIdByName(String countryName) {
        return dsl.fetchOne("""
                SELECT id
                    FROM countries
                    WHERE name = ?
                """, countryName
        ).into(Integer.class);
    }

    @Override
    public String getCountryNameById(Integer countryId) {
        return dsl.fetchOne("""
                SELECT name
                    FROM countries
                    WHERE id = ?
                """, countryId
        ).into(String.class);
    }

    @Override
    public void updateUser(UUID userId, UpdateUserForm updateUserForm) {
        dsl.query("""
                        UPDATE users
                        SET email = ?,
                            password_hash = ?,
                            country_id = (SELECT id FROM countries WHERE name = ?),
                            birth_date = ?
                        WHERE id = ?
                        """,
                updateUserForm.email(),
                updateUserForm.password(),
                updateUserForm.country(),
                updateUserForm.birthDate(),
                userId
        ).execute();
    }

    @Override
    public int userTokenCount(UUID userId) {
        return dsl.fetchOne("""
                SELECT get_user_token_balance(?);
                """, userId
        ).into(Integer.class);
    }
}
