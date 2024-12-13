package com.example.demo.entity;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    PENDING("pending"),
    APPROVED("approved"),
    CANCELLED("cancelled"),
    EXPIRED("expired");

    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }

}
