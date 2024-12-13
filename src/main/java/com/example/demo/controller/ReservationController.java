package com.example.demo.controller;

import com.example.demo.dto.ReservationRequestDto;
import com.example.demo.dto.ReservationResponseDto;
import com.example.demo.service.ReservationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> createReservation(
        @RequestBody ReservationRequestDto reservationRequestDto
    ) {
        ReservationResponseDto responseDto = reservationService.createReservation(
            reservationRequestDto.getItemId(),
            reservationRequestDto.getUserId(),
            reservationRequestDto.getStartAt(),
            reservationRequestDto.getEndAt()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/update-status")
    public void updateReservation(@PathVariable Long id, @RequestBody String status) {
        reservationService.updateReservationStatus(id, status);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> findAll() {
        return new ResponseEntity<>(reservationService.getReservations(), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReservationResponseDto>> searchAll(@RequestParam(required = false) Long userId,
                          @RequestParam(required = false) Long itemId) {
        return new ResponseEntity<>(
            reservationService.searchAndConvertReservations(userId, itemId),HttpStatus.OK
        );
    }
}
