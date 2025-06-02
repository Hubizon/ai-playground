package pl.edu.uj.tcs.aiplayground.service.repository;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.dto.CurrencyDto;
import pl.edu.uj.tcs.aiplayground.dto.LeaderboardDto;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.jooq.tables.records.UsersRecord;

import java.util.List;

public class TokenRepository implements ITokenRepository {
    private final DSLContext dsl;

    public TokenRepository(DSLContext dslContext) {
        this.dsl = dslContext;
    }

    @Override
    public void insertNewTokens(UserDto user, int amount) {
        dsl.query("""
                        INSERT INTO token_history(user_id, amount, event_type, description, timestamp)
                            VALUES (?,
                                    ?,
                                    (SELECT id FROM events WHERE name = ?),
                                    'to pole chb trzeba usunąć',
                                    now())
                        """,
                user.userId(),
                amount,
                "BoughtTokens"
        ).execute();
    }

    @Override
    public int getUserTokens(UserDto user) {
        int changed_tokens = dsl.fetchOne("""
                        SELECT SUM(amount)
                            FROM token_history
                            WHERE user_id = ?;
                        """,
                user.userId()
        ).into(Integer.class);
        int start_tokens = dsl.fetchOne("""
                        SELECT SUM(r.initial_tokens) 
                        FROM user_roles ur LEFT JOIN roles r ON ur.role_id = r.id
                        WHERE ur.user_id = ?;
                        """,
                user.userId()
        ).into(Integer.class);
        return changed_tokens + start_tokens;
    }

    @Override
    public List<CurrencyDto> getCurrencyList() {
        return dsl.fetch("""
                    SELECT name,conversion_rate FROM currencies;
                """
        ).into(CurrencyDto.class);
    }

}
