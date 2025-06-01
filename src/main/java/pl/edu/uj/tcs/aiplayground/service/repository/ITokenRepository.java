package pl.edu.uj.tcs.aiplayground.service.repository;

import pl.edu.uj.tcs.aiplayground.dto.UserDto;

public interface ITokenRepository {
    void insertNewTokens(UserDto user, int amount);
    int getUserTokens(UserDto user);
}
