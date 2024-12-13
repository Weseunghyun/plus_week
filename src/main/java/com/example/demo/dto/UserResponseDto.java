package com.example.demo.dto;

import lombok.Getter;

@Getter
public class UserResponseDto {
    private String email;
    private String nickname;
    private String role;

    public UserResponseDto(String email, String nickname, String role) {
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }
}
