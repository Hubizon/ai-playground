package pl.edu.uj.tcs.aiplayground.repository;

import pl.edu.uj.tcs.jooq.tables.records.UsersRecord;

public interface IUserRepository {
    boolean existUsername(String username);

    boolean existEmail(String email);

    UsersRecord findByUsername(String username);

    UsersRecord findByEmail(String email);

    void insertUser(UsersRecord user);

    void deleteUser(String id);
}
