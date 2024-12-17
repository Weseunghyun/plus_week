package com.example.demo.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PasswordEncoderTest {

    @Test
    void encode() {
        //given
        String password = "qwer1234!";

        //when
        String encodedPassword = PasswordEncoder.encode(password);

        //then
        assertNotEquals(password, encodedPassword);

        // BCrypt 해시가 특정 형식으로 시작하는지 확인, BCrypt 버전에 따라 $2a$ or $2b$ 으로 시작
        assertTrue(encodedPassword.startsWith("$2a$"));
    }

    @Test
    void matches() {
        //given
        String password = "qwer1234!";
        String encodedPassword = PasswordEncoder.encode(password);

        //when // then
        assertTrue(PasswordEncoder.matches(password, encodedPassword));
    }

    @Test
    void matchesWithIncorrectPassword() {
        //given
        String password = "qwer1234!";
        String wrongPassword = "wrongPassword!";
        String encodedPassword = PasswordEncoder.encode(password);

        //when // then
        assertFalse(PasswordEncoder.matches(wrongPassword, encodedPassword));
    }

    @Test
    void encodeMultiplePasswords() {
        //given
        String password1 = "password1";
        String password2 = "password2";

        //when
        String encodedPassword1 = PasswordEncoder.encode(password1);
        String encodedPassword2 = PasswordEncoder.encode(password2);

        //then
        assertNotEquals(encodedPassword1, encodedPassword2); // 서로 다른 비밀번호는 서로 다른 해시를 가져야 함
    }
}
