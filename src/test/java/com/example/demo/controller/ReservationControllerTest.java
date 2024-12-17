package com.example.demo.controller;

import com.example.demo.dto.ReservationRequestDto;
import com.example.demo.dto.ReservationResponseDto;
import com.example.demo.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("item/userId, startAt, endAt 정보로 예약 생성 시 성공적으로 responseDto 반환")
    void createReservation() throws Exception {
        // given
        Long itemId = 1L;
        Long userId = 1L;
        LocalDateTime startAt = LocalDateTime.now().withNano(0); // 나노초 조정
        LocalDateTime endAt = startAt.plusDays(1);

        ReservationRequestDto requestDto = new ReservationRequestDto(itemId, userId, startAt, endAt);

        ReservationResponseDto responseDto = new ReservationResponseDto(
            1L,
            "이름",
            "아이템이름",
            startAt,
            endAt
        );

        given(reservationService.createReservation(itemId, userId, startAt, endAt)).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.nickname").value("이름"))
            .andExpect(jsonPath("$.itemName").value("아이템이름"))
            .andExpect(jsonPath("$.startAt").value(startAt.toString())) // 나노초 7자리로 조정된 값 사용
            .andExpect(jsonPath("$.endAt").value(endAt.toString()));
    }


    @Test
    void updateReservation() throws Exception {
        // given
        Long reservationId = 1L;
        String status = "PENDING";

        ReservationResponseDto responseDto = new ReservationResponseDto(
            reservationId,
            "이름",
            "아이템이름",
            LocalDateTime.now().withNano(0),
            LocalDateTime.now().withNano(0).plusDays(1)
        );

        given(reservationService.updateReservationStatus(reservationId, status)).willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/reservations/{id}/update-status", reservationId)
                .contentType(MediaType.TEXT_PLAIN)
                .content(status)) // 단순 문자열 전달
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(reservationId))
            .andExpect(jsonPath("$.nickname").value("이름"))
            .andExpect(jsonPath("$.itemName").value("아이템이름"));
    }

    @Test
    @DisplayName("모든 예약 정보를 조회하면 성공적으로 responseDto 리스트 반환")
    void findAll() throws Exception {
        // given
        List<ReservationResponseDto> responseDtos = List.of(
            new ReservationResponseDto(
                1L, "이름1", "아이템1",
                LocalDateTime.now().withNano(0),
                LocalDateTime.now().withNano(0).plusDays(1)
            ),
            new ReservationResponseDto(
                2L, "이름2", "아이템2",
                LocalDateTime.now().withNano(0),
                LocalDateTime.now().withNano(0).plusDays(2))
        );

        given(reservationService.getReservations()).willReturn(responseDtos);

        // when & then
        mockMvc.perform(get("/reservations")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(responseDtos.size()))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].nickname").value("이름1"))
            .andExpect(jsonPath("$[0].itemName").value("아이템1"))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[1].nickname").value("이름2"))
            .andExpect(jsonPath("$[1].itemName").value("아이템2"));
    }


    @Test
    @DisplayName("userId, itemId로 예약을 검색하면 성공적으로 responseDto 리스트 반환")
    void searchAll() throws Exception {
        // given
        Long userId = 1L;
        Long itemId = 2L;

        List<ReservationResponseDto> responseDtos = List.of(
            new ReservationResponseDto(
                1L, "이름", "아이템",
                LocalDateTime.now().withNano(0),
                LocalDateTime.now().withNano(0).plusDays(1)
            )
        );

        given(reservationService.searchAndConvertReservations(userId, itemId)).willReturn(responseDtos);

        // when & then
        mockMvc.perform(get("/reservations/search")
                .param("userId", userId.toString())
                .param("itemId", itemId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(responseDtos.size()))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].nickname").value("이름"))
            .andExpect(jsonPath("$[0].itemName").value("아이템"));
    }

}