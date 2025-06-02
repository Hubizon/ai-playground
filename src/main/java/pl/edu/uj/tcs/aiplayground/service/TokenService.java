package pl.edu.uj.tcs.aiplayground.service;

import pl.edu.uj.tcs.aiplayground.dto.CurrencyDto;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.service.repository.ITokenRepository;

import java.util.List;

public class TokenService {
    private final ITokenRepository tokenRepository;

    public TokenService(ITokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void insertNewTokens(UserDto user, int amount) throws DatabaseException {
        try {
            tokenRepository.insertNewTokens(user, amount);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public int getUserTokens(UserDto user) throws DatabaseException {
        try {
            return tokenRepository.getUserTokens(user);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public List<CurrencyDto> getCurrencyList() throws DatabaseException {
        try {
            return tokenRepository.getCurrencyList();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
