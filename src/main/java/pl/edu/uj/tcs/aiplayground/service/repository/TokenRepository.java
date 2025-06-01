package pl.edu.uj.tcs.aiplayground.service.repository;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;

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
                                    (SELECT id FROM events WHERE name = newTokens),
                                    'to pole chb trzeba usunąć',
                                    now())
                        """,
                user.userId(),
                amount
        ).execute();
    }
    @Override
    public int getUserTokens(UserDto user){
        return 69; //TODO Fetch it from database
    }

}
