package pl.edu.uj.tcs.aiplayground.service.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {
    @Test
    void hashedPasswordShouldNotBeNullNorEmpty() {
        String password = "password";
        String hashed = PasswordHasher.hash(password);
        assertNotNull(hashed);
        assertFalse(hashed.isEmpty());
    }

    @Test
    void hashedPasswordShouldBeValid() {
        String password1 = "password";
        String hashed1 = PasswordHasher.hash(password1);
        assertTrue(PasswordHasher.verify(password1, hashed1));

        String password2 = "-+123asd./,,ASDfs''@#$#!@#!*)~~NDJKWS'\\\\d@#d==~d$b__>;;d.sal!#!";
        String hashed2 = PasswordHasher.hash(password2);
        assertTrue(PasswordHasher.verify(password2, hashed2));
    }
}
