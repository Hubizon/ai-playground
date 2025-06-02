package pl.edu.uj.tcs.aiplayground.service.repository;

import pl.edu.uj.tcs.aiplayground.dto.CurrencyDto;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;

import java.util.List;

public interface ITokenRepository {
    void insertNewTokens(UserDto user, int amount);
    int getUserTokens(UserDto user);

    List<CurrencyDto> getCurrencyList();
}
