package pl.edu.uj.tcs.aiplayground.service.repository;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.dto.CurrencyDto;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;

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
                                    'Bought ' || ? || ' tokens',
                                    now())
                        """,
                user.userId(),
                amount,
                "BoughtTokens",
                amount
        ).execute();
    }

    @Override
    public int getUserTokens(UserDto user) {
        return dsl.fetchOne("""
                        SELECT get_user_token_balance(?);
                        """,
                user.userId()
        ).into(Integer.class);
    }

    @Override
    public List<CurrencyDto> getCurrencyList() {
        return dsl.fetch("""
                    SELECT name, conversion_rate FROM currencies;
                """
        ).into(CurrencyDto.class);
    }
}
