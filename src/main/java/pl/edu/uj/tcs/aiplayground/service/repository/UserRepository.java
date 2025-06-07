package pl.edu.uj.tcs.aiplayground.service.repository;

import javafx.beans.property.StringProperty;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
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
                        SET first_name = ?,
                            last_name = ?,
                            password_hash = ?,
                            country_id = (SELECT id FROM countries WHERE name = ?),
                            birth_date = ?
                        WHERE id = ?
                        """,
                updateUserForm.firstName(),
                updateUserForm.lastName(),
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

    @Override
    public boolean isUserAdmin(UUID userId) {
        return Boolean.TRUE.equals(dsl.fetchOne("""
                SELECT EXISTS (
                    SELECT 1 FROM user_roles ur
                    JOIN roles r ON ur.role_id = r.id
                    WHERE ur.user_id = ? AND ur.is_active = true AND r.name = 'Administrator'
                )
                """, userId
        ).into(Boolean.class));
    }

    @Override
    public List<String> getUsernamesWithoutUser(UUID userId) {
        return dsl.fetch("""
                SELECT username FROM users WHERE id <> ?
                """, userId
        ).into(String.class);
    }

    @Override
    public List<String> getRoleNames() {
        return dsl.fetch("""
                SELECT name FROM roles
                """).into(String.class);
    }

    @Override
    public String getUserRole(StringProperty chosenUser) {
        return dsl.fetchOne("""
                SELECT r.name FROM users u
                JOIN user_roles ur ON u.id = ur.user_id AND ur.is_active = true
                JOIN roles r ON r.id = ur.role_id
                WHERE u.username = ?
                """, chosenUser.get()
        ).into(String.class);
    }

    @Override
    public void setRoleForUser(String username, String role) {
        dsl.transaction(configuration -> {
            DSLContext context = DSL.using(configuration);

            context.execute("""
                        UPDATE user_roles ur
                        SET is_active = FALSE
                        FROM users u
                        WHERE ur.user_id = u.id
                          AND u.username = ?
                          AND ur.is_active = TRUE
                    """, username);

            context.execute("""
                        INSERT INTO user_roles (user_id, role_id, is_active)
                        SELECT u.id, r.id, TRUE
                        FROM users u
                        JOIN roles r ON r.name = ?
                        WHERE u.username = ?
                    """, role, username);
        });
    }

    @Override
    public void deleteUser(UUID userId) {
        dsl.query("""
                        DELETE FROM users
                        WHERE id = ?;
                        """,
                userId
        ).execute();
    }

    @Override
    public UserDto getUser(UUID userId) {
        return dsl.fetchOne("""
                        SELECT users.id AS userId, username, first_name AS firstName, last_name AS lastName,
                               email, countries.name AS countryName, birth_date AS birthDate
                            FROM users
                            JOIN countries ON countries.id = users.country_id
                            WHERE users.id = ?;
                        """,
                userId
        ).into(UserDto.class);
    }
}
