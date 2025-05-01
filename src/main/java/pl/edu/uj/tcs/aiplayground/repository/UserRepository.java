package pl.edu.uj.tcs.aiplayground.repository;

import org.example.jooq.tables.Users;
import org.example.jooq.tables.records.UsersRecord;
import org.jooq.DSLContext;

import java.util.UUID;

import static org.example.jooq.Tables.USERS;

public class UserRepository {
    private final DSLContext dsl;

    public UserRepository(DSLContext dslContext) {
        this.dsl = dslContext;
    }

    public UsersRecord findByUsername(String username) {
        return dsl.select()
                .from(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOneInto(UsersRecord.class);
    }

    public UsersRecord findByEmail(String email) {
        return dsl.select()
                .from(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOneInto(UsersRecord.class);
    }

    public void insertUser(UsersRecord user) {
        user.attach(dsl.configuration());
        user.store();
    }

    public void deleteUser(String id) {
        dsl.deleteFrom(USERS)
                .where(USERS.ID.eq(UUID.fromString(id)))
                .execute();
    }
}
