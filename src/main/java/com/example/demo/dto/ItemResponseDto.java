package com.example.demo.dto;

import lombok.Getter;

@Getter
public class ItemResponseDto {

    private Long id;

    private String name;

    private String description;

    public ItemResponseDto(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
