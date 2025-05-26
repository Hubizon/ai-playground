package pl.edu.uj.tcs.aiplayground.service.repository;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;

public class JooqFactory {
    private static DSLContext dslContext;

    public static DSLContext getDSLContext() {
        if (dslContext == null) {
            try {
                Connection connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/aiplayground",
                        "aiplayground",
                        "aiplayground"
                );
                dslContext = DSL.using(connection, SQLDialect.POSTGRES);
            } catch (Exception e) {
                throw new RuntimeException("Can't connect to the database", e);
            }
        }
        return dslContext;
    }
}
